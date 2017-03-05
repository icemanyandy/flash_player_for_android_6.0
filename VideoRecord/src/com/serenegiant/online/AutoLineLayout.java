package com.serenegiant.online;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 自动换行控件
 * 
 * @author cailiming
 * 
 */
public class AutoLineLayout extends ViewGroup {

	private int lineHeight;
	private int horizontalSpacing;
	private int verticalSpacing;
	private int lines = 1;

	public int getLines() {
		return lines;
	}
	
	public int getLineHeight() {
		return lineHeight;
	}
	public AutoLineLayout(Context context) {
		super(context);
	}

	public AutoLineLayout(Context context, int horizontalSpacing, int verticalSpacing) {
		super(context);
		this.horizontalSpacing = horizontalSpacing;
		this.verticalSpacing = verticalSpacing;
	}

	public AutoLineLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TypedArray styledAttributes = context.obtainStyledAttributes(attrs,
		// R.styleable.PredicateLayout);
		// horizontalSpacing =
		// styledAttributes.getDimensionPixelSize(R.styleable.PredicateLayout_item_h_space,
		// DEFAULT_HORIZONTAL_SPACING);
		// verticalSpacing =
		// styledAttributes.getDimensionPixelSize(R.styleable.PredicateLayout_item_v_space,
		// DEFAULT_VERTICAL_SPACING);
	}

	public AutoLineLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setHorizontalSpacing(int horizontalSpacing) {
		this.horizontalSpacing = horizontalSpacing;
	}

	public void setVerticalSpacing(int verticalSpacing) {
		this.verticalSpacing = verticalSpacing;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		assert (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
		final int count = getChildCount();
		int lineheightTmp = 0;
		// int line_height = 22;

		int xpos = getPaddingLeft();
		int ypos = getPaddingTop();

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				child.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),
						MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));

				final int childw = child.getMeasuredWidth();
				lineheightTmp = Math.max(lineheightTmp, child.getMeasuredHeight() + verticalSpacing);

				if (xpos + childw > width) {
					xpos = getPaddingLeft();
					ypos += lineheightTmp;
				}

				xpos += childw + horizontalSpacing;
			}
		}
		this.lineHeight = lineheightTmp;

		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
			height = ypos + lineheightTmp;

		} else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
			if (ypos + lineheightTmp < height) {
				height = ypos + lineheightTmp;
			}
		}
		setMeasuredDimension(width, height);
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(1, 1); // default of 1px spacing
	}

	@Override
	protected boolean checkLayoutParams(LayoutParams p) {
		return p instanceof LayoutParams;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int count = getChildCount();
		final int width = r - l;
		int xpos = getPaddingLeft();
		int ypos = getPaddingTop();

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				final int childw = child.getMeasuredWidth();
				final int childh = child.getMeasuredHeight();
				// final int childh = 24;
				if (xpos + childw > width) {
					xpos = getPaddingLeft();
					ypos += lineHeight;
					lines++;
				}
				child.layout(xpos, ypos, xpos + childw, ypos + childh);
				xpos += childw + horizontalSpacing;
			}
		}
	}
	
     public void removeAllViews(){
    	 super.removeAllViews();
    	 lines=1;
     }

}