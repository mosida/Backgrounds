package com.galeapp.backgrounds.views;

import com.galeapp.backgrounds.Constants;
import com.galeapp.backgrounds.R;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class ResultView extends ImageView implements OnTouchListener {

	public static final String TAG = "ResultView";
	// resultView width, height
	int picWidth;
	int picHeight;
	// device width, height
	int deviceW;
	int deviceH;
	// drawRect
	public Rect lineRect = new Rect();
	//
	int lastX = 0;
	int lastY = 0;
	public int type = Constants.STANDARD_TYPE;

	public ResultView(Context context, AttributeSet attrs) {
		super(context, attrs);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		deviceW = dm.widthPixels;
		deviceH = dm.heightPixels;

		setOnTouchListener(this);
	}

	@Override
	public void onDraw(Canvas canvas) {
		Log.i(TAG, "onDraw");
		super.onDraw(canvas);

		Paint paint = new Paint();
		paint.setARGB(255, 254, 102, 0);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawRect(lineRect, paint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Log.i(TAG, "onMesuere width:" + widthMeasureSpec + " height:"
				+ heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		Log.i(TAG, "onLayout");
		super.onLayout(changed, left, top, right, bottom);
		Log.i(TAG, "changed:" + changed + " left:" + left + " top:" + top
				+ " right:" + right + " bottom:" + bottom);

		picWidth = right - left;
		picHeight = bottom - top;

		reflashResultView(type);
	}

	public void setType(int type) {
		this.type = type;
	}

	// 更新界面
	public void reflashResultView(int type) {

		this.type = type;
		if (type == Constants.STANDARD_TYPE) {
			setLineRectStandardMode();
		} else if (type == Constants.FIXED_TYPE) {
			setLineRectFixedMode();
		} else {
			setLineRectExtireMode();
		}
		invalidate();
	}

	public void resetLineRectByWidth(Rect rect, int width) {
		lineRect.left = (picWidth - width) / 2;
		lineRect.right = lineRect.left + width;

		if(lineRect.left<0){
			lineRect.left=0;
			lineRect.right=picWidth-1;
		}
		
		lineRect.top = 0;
		lineRect.bottom = picHeight - 1;
	}

	public void resetLineRectByHeight(Rect rect, int height) {
		lineRect.top = (picHeight - height) / 2;
		lineRect.bottom = lineRect.top + height;
		if(lineRect.top<0){
			lineRect.top=0;
			lineRect.bottom=picHeight-1;
		}
		lineRect.left = 0;
		lineRect.right = picWidth - 1;
	}

	// 标准模式
	public void setLineRectStandardMode() {
		if (picWidth >= picHeight) {
			int drawW = picHeight * 6 / 5;
			if (drawW > picWidth) {
				drawW = picWidth - 1;
			}
			resetLineRectByWidth(lineRect, drawW);
		} else {
			int drawH = picWidth * 5 / 6;
			if (drawH > picHeight) {
				drawH = picHeight - 1;
			}
			resetLineRectByHeight(lineRect, drawH);
		}
	}

	// 竖屏模式
	public void setLineRectFixedMode() {
		int drawW = picHeight * deviceW / deviceH;
		resetLineRectByWidth(lineRect, drawW);
	}

	// 全框模式
	public void setLineRectExtireMode() {
		lineRect.left = 0;
		lineRect.top = 0;
		lineRect.right = picWidth - 1;
		lineRect.bottom = picHeight - 1;
	}

	@Override
	public void setImageBitmap(Bitmap bitmap) {
		Log.i(TAG, "setImageBitmap");
		super.setImageBitmap(bitmap);
		type = Constants.ENTIRE_TYPE;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int pointX = (int) event.getRawX();
		int pointY = (int) event.getRawY();

		int changeX = 0;
		int changeY = 0;

		int keyCode = event.getAction();
		switch (keyCode) {
		case MotionEvent.ACTION_DOWN:
			lastX = pointX;
			lastY = pointY;
			break;
		case MotionEvent.ACTION_MOVE:
			int left = lineRect.left;
			int right = lineRect.right;
			int top = lineRect.top;
			int bottom = lineRect.bottom;

			changeX = pointX - lastX;
			changeY = pointY - lastY;
			// 向右移动
			if (changeX > 0) {
				if (right + changeX >= picWidth - 1) {
					changeX = 0;
				}
			} else if (changeX < 0) {// 向左移动
				if (left + changeX <= 0) {
					changeX = 0;
				}
			}
			// 向下移动
			if (changeY > 0) {
				if (bottom + changeY >= picHeight - 1) {
					changeY = 0;
				}
			} else if (changeY < 0) {// 向上移动
				if (top + changeY <= 0) {
					changeY = 0;
				}
			}
			lineRect.offsetTo(lineRect.left + changeX, lineRect.top + changeY);
			lastX = pointX;
			lastY = pointY;

			break;
		case MotionEvent.ACTION_UP:
			lastX = 0;
			lastY = 0;
			break;
		default:
			break;
		}

		invalidate();
		return true;
	}

	// 获取虚线在图片比例位置left
	public float getScaleLeft() {
		Log.i(TAG, "lineRect left:" + lineRect.left + " picWidth:" + picWidth
				+ " sacle" + (float) lineRect.left / picWidth);
		return (float) lineRect.left / picWidth;
	}

	// 获取虚线在图片比例位置top
	public float getScaleTop() {
		Log.i(TAG, "lineRect top:" + lineRect.top + " picHeight:" + picHeight
				+ " sacle" + (float) lineRect.top / picHeight);
		return (float) lineRect.top / picHeight;
	}

	// 获取虚线在图片比例位置right
	public float getScaleRight() {
		Log.i(TAG, "lineRect right:" + lineRect.right + " picWidth:" + picWidth
				+ " sacle" + (float) lineRect.right / picWidth);
		return (float) lineRect.right / picWidth;
	}

	// 获取虚线在图片比例位置bottom
	public float getScaleBottom() {
		Log.i(TAG, "lineRect bottom:" + lineRect.left + " picHeight:"
				+ picHeight + " sacle" + (float) lineRect.bottom / picHeight);
		return (float) lineRect.bottom / picHeight;
	}
}
