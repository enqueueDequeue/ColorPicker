package com.example.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.ColorInt;

public class ColorPicker extends ViewGroup {
  private static final int COLOR_WHEEL_RADIUS = 400;
  private static final float COLOR_WHEEL_SATURATION_POWER = 1.3f;
  private static final int COLOR_WHEEL_IMAGE_SIZE = 1 + 2 * COLOR_WHEEL_RADIUS;
  public static final Bitmap COLOR_WHEEL_BITMAP = Bitmap.createBitmap(COLOR_WHEEL_IMAGE_SIZE, COLOR_WHEEL_IMAGE_SIZE, Bitmap.Config.ARGB_8888);
  private static final int[][] COLOR_WHEEL = new int[COLOR_WHEEL_IMAGE_SIZE][COLOR_WHEEL_IMAGE_SIZE]; // 1d for easier sorting

  private float selectorX = COLOR_WHEEL_RADIUS, selectorY = COLOR_WHEEL_RADIUS;
  private int halfSelectorWidth, halfSelectorHeight;

  static {
    float[] hsv = new float[3];
    hsv[2] = 1f;

    for (int x = -COLOR_WHEEL_RADIUS; x <= COLOR_WHEEL_RADIUS; x++) {
      for (int y = -COLOR_WHEEL_RADIUS; y <= COLOR_WHEEL_RADIUS; y++) {
        int tx = x + COLOR_WHEEL_RADIUS, ty = y + COLOR_WHEEL_RADIUS;
        double a = atan(y, x);

        hsv[0] = (float) Math.toDegrees(a); // hue is angle
        hsv[1] = (float) (Math.sqrt((x * x) + (y * y)) / COLOR_WHEEL_RADIUS); // saturation is radius

        hsv[1] = (float) Math.pow(hsv[1], COLOR_WHEEL_SATURATION_POWER);

        if (1f < hsv[1]) // to draw a circle
          continue;

        COLOR_WHEEL[tx][ty] = Color.HSVToColor(hsv);
      }
    }

    for (int x = 0; x < COLOR_WHEEL_IMAGE_SIZE; x++) {
      for (int y = 0; y < COLOR_WHEEL_IMAGE_SIZE; y++) {
        COLOR_WHEEL_BITMAP.setPixel(x, y, COLOR_WHEEL[x][y]);
      }
    }
  }

  private static double atan(double y, double x) {
    double a = Math.atan2(Math.abs(y), Math.abs(x));

    if (x >= 0 && y >= 0) {
      return +a;
    } else if (x <=0 && y <= 0) {
      return Math.PI + a;
    } else if (x < 0 && y > 0) {
      return Math.PI - a;
    } else {
      return (2 * Math.PI) - a;
    }
  }

  private ImageView palette;
  private ImageView selector;
  private OnColorSelectedListener onColorSelectedListener;

  public ColorPicker(Context context) {
    super(context);
    init(context);
  }

  public ColorPicker(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public ColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    Drawable selector = context.getDrawable(R.drawable.ic_panorama_fish_eye_black_24dp);

    this.palette = new ImageView(context);
    this.selector = new ImageView(context);

    this.selector.setImageDrawable(selector);
    this.palette.setImageBitmap(COLOR_WHEEL_BITMAP);

    if (null != selector) {
      halfSelectorHeight = selector.getIntrinsicHeight() / 2;
      halfSelectorWidth = selector.getIntrinsicWidth() / 2;
    }

    addView(this.palette);
    addView(this.selector);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int selectorLeft, selectorTop, selectorRight, selectorBottom;

    selectorLeft   = -halfSelectorWidth;
    selectorTop    = -halfSelectorHeight;
    selectorRight  =  halfSelectorWidth;
    selectorBottom =  halfSelectorHeight;

    palette.layout(getPaddingStart() + halfSelectorWidth, getPaddingTop() + halfSelectorHeight, r - l - getPaddingEnd() - halfSelectorWidth, b - t - getPaddingBottom() - halfSelectorHeight);
    selector.layout(selectorLeft, selectorTop, selectorRight, selectorBottom);

    selector.setTranslationX(COLOR_WHEEL_RADIUS + halfSelectorWidth);
    selector.setTranslationY(COLOR_WHEEL_RADIUS + halfSelectorHeight);
  }

  @ColorInt
  private int getPointColor(float x, float y) {
    float[] hsv = new float[3];
    double a = atan(y, x);

    hsv[0] = (float) Math.toDegrees(a); // hue is angle

    hsv[1] = (float) (Math.sqrt((x * x) + (y * y)) / COLOR_WHEEL_RADIUS); // saturation is radius
    hsv[1] = (float) Math.pow(hsv[1], COLOR_WHEEL_SATURATION_POWER);

    hsv[2] = 1f;

    return Color.HSVToColor(hsv);
  }

  private Point getColorPoint(@ColorInt int color) {
    // h is angle(a), s is radius (r), v should be 1 (since only 1 is passed during construction)
    int x, y;
    double a, r;
    float s;
    float[] hsv = new float[3];

    hsv[2] = 1f;

    Color.colorToHSV(color, hsv);

    a = Math.toRadians(hsv[0]);

    s = hsv[1];
    s = (float) Math.pow(s, 1d / COLOR_WHEEL_SATURATION_POWER);
    r = s * COLOR_WHEEL_RADIUS;

    x = (int) (r * Math.cos(a));
    y = (int) (r * Math.sin(a));

    Log.w("getting point", "h: "+ hsv[0] +  ", s: " + s + ", v: " + hsv[2]);
    Log.w("getting point", "x: "+ x +  ", y:" + y + ", r: " + r);

    return new Point(x, y);
  }

  public void setColor(@ColorInt int color) {
    Point loc = getColorPoint(color);

    selectorX = loc.x;
    selectorY = loc.y;

    selector.setTranslationX(selectorX + COLOR_WHEEL_RADIUS);
    selector.setTranslationY(selectorY + COLOR_WHEEL_RADIUS);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(COLOR_WHEEL_IMAGE_SIZE + (2 * halfSelectorWidth), COLOR_WHEEL_IMAGE_SIZE + (2 * halfSelectorHeight));
  }

  public void setOnColorSelectedListener(OnColorSelectedListener onColorSelectedListener) {
    this.onColorSelectedListener = onColorSelectedListener;
  }

  @Override
  public void setOnTouchListener(OnTouchListener l) {
    throw new IllegalStateException();
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    handleTouch(ev);

    if (MotionEvent.ACTION_UP == ev.getActionMasked()) {
      performClick();

      if (null != onColorSelectedListener) {
        onColorSelectedListener.onColorSelected(getPointColor(selectorX - COLOR_WHEEL_RADIUS, selectorY - COLOR_WHEEL_RADIUS));
      }
    }
    return true;
  }

  @Override
  public boolean performClick() {
    return super.performClick();
  }

  private void handleTouch(MotionEvent ev) {
    double r, x, y;
    selectorX = ev.getX();
    selectorY = ev.getY();

    x = selectorX - COLOR_WHEEL_RADIUS;
    y = selectorY - COLOR_WHEEL_RADIUS;

    r = Math.sqrt((x * x) + (y * y));

    if (r >= COLOR_WHEEL_RADIUS) {
      Log.w("ookla", "crossed the boundary");
      double a = atan(y, x);

      selectorX = (float) ((COLOR_WHEEL_RADIUS - halfSelectorWidth) * Math.cos(a));
      selectorY = (float) ((COLOR_WHEEL_RADIUS - halfSelectorHeight) * Math.sin(a));

      selectorX = selectorX + COLOR_WHEEL_RADIUS + halfSelectorWidth;
      selectorY = selectorY + COLOR_WHEEL_RADIUS + halfSelectorHeight;
    }

    selector.setTranslationX(selectorX);
    selector.setTranslationY(selectorY);
  }

  public interface OnColorSelectedListener {
    void onColorSelected(@ColorInt int color);
  }
}
