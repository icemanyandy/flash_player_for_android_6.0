package com.aspsine.multithreaddownload.core;

import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.aspsine.multithreaddownload.Constants;
import com.aspsine.multithreaddownload.DownloadException;
import com.aspsine.multithreaddownload.architecture.ConnectTask;
import com.aspsine.multithreaddownload.architecture.DownloadStatus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Aspsine on 2015/7/20.
 */
public class ConnectTaskImpl implements ConnectTask {
    private final String mUri;
    private final OnConnectListener mOnConnectListener;

    private volatile int mStatus;

    private volatile long mStartTime;

    public ConnectTaskImpl(String uri, OnConnectListener listener) {
        this.mUri = uri;
        this.mOnConnectListener = listener;
    }

    @Override
    public void pause() {
        mStatus = DownloadStatus.STATUS_PAUSED;
    }

    public void cancel() {
        mStatus = DownloadStatus.STATUS_CANCELED;
    }

    @Override
    public boolean isConnecting() {
        return mStatus == DownloadStatus.STATUS_CONNECTING;
    }

    @Override
    public boolean isConnected() {
        return mStatus == DownloadStatus.STATUS_CONNECTED;
    }

    @Override
    public boolean isPaused() {
        return mStatus == DownloadStatus.STATUS_PAUSED;
    }

    @Override
    public boolean isCanceled() {
        return mStatus == DownloadStatus.STATUS_CANCELED;
    }

    @Override
    public boolean isFailed() {
        return mStatus == DownloadStatus.STATUS_FAILED;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        mStatus = DownloadStatus.STATUS_CONNECTING;
        mOnConnectListener.onConnecting();
        try {
            executeConnection();
        } catch (DownloadException e) {
            handleDownloadException(e);
        }
    }

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

    };

    private static void trustAllHosts() {
        final String TAG = "trustAllHosts";
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i(TAG, "checkClientTrusted");
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i(TAG, "checkServerTrusted");
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeConnection() throws DownloadException {
        mStartTime = System.currentTimeMillis();
        HttpURLConnection httpConnection = null;
        final URL url;
        try {
            url = new URL(mUri);
        } catch (MalformedURLException e) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED, "Bad url.", e);
        }
        try {
            // trust all hosts
            if (url.getProtocol().toUpperCase().equals("HTTPS")) {
                trustAllHosts();
            }
            httpConnection = (HttpURLConnection) url.openConnection();
            if(httpConnection instanceof HttpsURLConnection){
                ((HttpsURLConnection) httpConnection).setHostnameVerifier(DO_NOT_VERIFY);
            }
            httpConnection.setConnectTimeout(Constants.HTTP.CONNECT_TIME_OUT);
            httpConnection.setReadTimeout(Constants.HTTP.READ_TIME_OUT);
            httpConnection.setRequestMethod(Constants.HTTP.GET);
            httpConnection.setRequestProperty("Range", "bytes=" + 0 + "-");
            final int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                parseResponse(httpConnection, false);
            } else if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                parseResponse(httpConnection, false);//说是可以多线程下载
            } else {
                throw new DownloadException(DownloadStatus.STATUS_FAILED, "UnSupported response code:" + responseCode);
            }
        } catch (ProtocolException e) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED, "Protocol error", e);
        } catch (IOException e) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED, "IO error", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    private void parseResponse(HttpURLConnection httpConnection, boolean isAcceptRanges) throws DownloadException {

        final long length;
        String contentLength = httpConnection.getHeaderField("Content-Length");
        if (TextUtils.isEmpty(contentLength) || contentLength.equals("0") || contentLength.equals("-1")) {
            length = httpConnection.getContentLength();
        } else {
            length = Long.parseLong(contentLength);
        }

        if (length <= 0) {
             //throw new DownloadException(DownloadStatus.STATUS_FAILED, "length <= 0");
        }

        checkCanceledOrPaused();

        //Successful
        mStatus = DownloadStatus.STATUS_CONNECTED;
        final long timeDelta = System.currentTimeMillis() - mStartTime;
        mOnConnectListener.onConnected(timeDelta, length, isAcceptRanges);
    }

    private void checkCanceledOrPaused() throws DownloadException {
        if (isCanceled()) {
            // cancel
            throw new DownloadException(DownloadStatus.STATUS_CANCELED, "Connection Canceled!");
        } else if (isPaused()) {
            // paused
            throw new DownloadException(DownloadStatus.STATUS_PAUSED, "Connection Paused!");
        }
    }

    private void handleDownloadException(DownloadException e) {
        switch (e.getErrorCode()) {
            case DownloadStatus.STATUS_FAILED:
                synchronized (mOnConnectListener) {
                    mStatus = DownloadStatus.STATUS_FAILED;
                    mOnConnectListener.onConnectFailed(e);
                }
                break;
            case DownloadStatus.STATUS_PAUSED:
                synchronized (mOnConnectListener) {
                    mStatus = DownloadStatus.STATUS_PAUSED;
                    mOnConnectListener.onConnectPaused();
                }
                break;
            case DownloadStatus.STATUS_CANCELED:
                synchronized (mOnConnectListener) {
                    mStatus = DownloadStatus.STATUS_CANCELED;
                    mOnConnectListener.onConnectCanceled();
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown state");
        }
    }
}
