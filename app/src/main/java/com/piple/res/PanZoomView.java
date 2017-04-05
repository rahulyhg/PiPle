package com.piple.res;



import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.HashMap;
import java.util.Map;


/**
 * Class PanZoomView
 *      extends View
 *
 * Class creating a gigantic window in which the user has the ability to zoom in, out by pinching and double tapping
 * it resizes the image and also allow the user to scroll in the X and Y axis.
 */
public class PanZoomView
        extends
            View
{



    /// RESOURCES ///
    /**
     * on the X and Y axis
     */
    static protected Map<String, Integer> totalscreensize = new HashMap<String, Integer>();

    /**
     *
     * Information on the availability of certain Actions by the User
     *  The next three are set by calling supportsPan, supportsZoom, ...
     */
    static protected final boolean ScaleAtFocusPoint = false;
    protected boolean mSupportsPan = true;
    protected boolean mSupportsZoom = true;
    protected boolean mSupportsScaleAtFocus = true;
    protected boolean zooming=false; //to have a smoother zoom

    /**
     * the iamge that is going to be drawn
     */
    protected Drawable mSampleImage;
    protected Context mContext;
    /**
     *
     * position of the screen
     */
    protected float mPosX;
    protected float mPosY;

    /**
     *
     * initial displacement values
     */
    protected float mPosX0 = 0;
    protected float mPosY0 = 0;

    /**
     *
     * where is the pinch focused
     */
    protected float mFocusX;
    protected float mFocusY;

    /**
     *
     * the last place the user(s finger was on
     */
    protected float mLastTouchX;
    protected float mLastTouchY;

    protected static final int INVALID_POINTER_ID = -1;

    // The ‘active pointer’ is the one currently moving our object.
    protected int mActivePointerId = INVALID_POINTER_ID;

    protected ScaleGestureDetector mScaleDetector;
    /**
     *
     * defininf how much we have zoomed
     */
    protected float mScaleFactor = 1.f;

    /**
     *
     * the id on where we are going to draw ( redefined in the system )
     */
    static protected final int DefaultDrawableId = 0x7f020053;

    /// CONSTRUCTORS ///

    public PanZoomView (Context context) {
        this(context, null, 0);
    }

    public PanZoomView (Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanZoomView (Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;
        setupToDraw (context, attrs, defStyle);
        setupScaleDetector (context, attrs, defStyle);
    }



    /// METHODS ///

    /**
     * Calculate the inSampleSize to use in BitmapFactory.Options in order
     * to load a drawable resource into a bitmap of the specified size.
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }
        return inSampleSize;
    }



    /**
     * Decode a resource into a bitmap of the specified size.
     *
     * @param res
     *      the ressource used
     * @param reqHeight
     *        height of the Bm
     * @param reqWidth
     *         width of the Bm
     * @param resId
     *      id of the ressource
     *
     *
     * @return the bitmap
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight)
    {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }



    /**
     * Do whatever drawing is appropriate for this view.
     * The canvas object is already set up to be drawn on. That means that all translations and scaling
     * operations have already been done.
     *
     * @param canvas Canvas
     * @return void
     */
    public void drawOnCanvas (Canvas canvas)
    {

    }



    /**
     * onDraw
     * will call translate and scale functions to the canvas
     * according to the information gotten
     * by calculating it ith pos, lastouch, ...
     *
     */
    @Override public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        canvas.save();

        float x = 0, y = 0;
        x = mPosX + mPosX0;
        y = mPosY + mPosY0;
        if (mSupportsZoom || mSupportsPan) {
            if (mSupportsPan && !mSupportsZoom) {
                canvas.translate(x, y);
                canvas.scale(mScaleFactor, mScaleFactor);
                Log.d ("Multitouch", "+p-z x, y : " + x + " " + y);
            } else if (mSupportsPan && mSupportsZoom) {
                if (mScaleDetector.isInProgress()) {
                    // Pinch zoom is in progress
                    // if (mSupportsPan) canvas.translate(mPosX, mPosY);
                    //TODO get ancient focus and save it for zooming to perform an additional zoom to smooth the action
                    mFocusX = mScaleDetector.getFocusX ();
                    mFocusY = mScaleDetector.getFocusY ();
                    zooming=true;
                    canvas.translate(x, y);
                    canvas.scale(mScaleFactor, mScaleFactor);
                    Log.d ("Multitouch", "+p-z x, y : " + x + " " + y);
                } else {
                    if(zooming){

                    }
                    // Pinch zoom is not in progress. Just do translation of canvas at whatever the current scale is.
                    canvas.translate(x, y);
                    canvas.scale(mScaleFactor, mScaleFactor);
                }
            } else if (mSupportsZoom) {
                // Not working perfectly when mPosX0 is set.
                canvas.translate(mPosX0, mPosY0);         // Translate canvas so center point can be chosen.
                mFocusX = mScaleDetector.getFocusX ();
                mFocusY = mScaleDetector.getFocusY ();
                canvas.scale(mScaleFactor, mScaleFactor, mFocusX -mPosX0, mFocusY -mPosY0);
                Log.d ("Multitouch", "+p-z x, y : " + x + " " + y);
            }
        }

        // Do whatever drawing is appropriate for this class
        drawOnCanvas (canvas);

        canvas.restore();
    }



    /**
     * Handle touch and multitouch events so panning and zooming can be supported.
     *
     *
     */
    @Override public boolean onTouchEvent(MotionEvent ev)
    {

        // If we are not supporting either zoom or pan, return early.
        if (!mSupportsZoom && !mSupportsPan) return false;

        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                // Only move if the view supports panning and
                // ScaleGestureDetector isn't processing a gesture.
                if (mSupportsPan && !mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    // TODO : il faut recuperer la taille de l'écran et l'on peut ensuite gérer des limites de déplacement
                    //if(mPosX<)
                    mPosX += dx;
                    mPosY += dy;
                    //mFocusX = mPosX;
                    //mFocusY = mPosY;

                    invalidate();
                }

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
    }



    /**
     * Return the resource id of the sample image.
     *
     * @return int
     */
    public int sampleDrawableId () {
        return DefaultDrawableId;
    }



    /**
     * This method sets up the scale detector object used by the view. It is called by the constructor.
     *
     * @return void
     */
    protected void setupScaleDetector (Context context, AttributeSet attrs, int defStyle) {
        // Create our ScaleGestureDetector
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }



    /**
     * This method performs whatever set up is necessary to do drawing. It is called by the constructor.
     * The default implementation checks to see if both panning and zooming are supported.
     * And it also locates the sample drawable resource by calling sampleDrawableId.
     * If that method returns 0, the sample image is not set up.
     *
     * @return void
     */
    protected void setupToDraw (Context context, AttributeSet attrs, int defStyle) {
        mSupportsPan = supportsPan ();
        mSupportsZoom = supportsZoom ();
        mSupportsScaleAtFocus = supportsScaleAtFocusPoint ();

        int resourceId = sampleDrawableId ();
        if (resourceId == 0) return;
        mSampleImage = context.getResources().getDrawable (resourceId);
        mSampleImage.setBounds(0, 0, mSampleImage.getIntrinsicWidth(), mSampleImage.getIntrinsicHeight());
    }



    public boolean supportsPan () {
        return true;
    }



    /**
     * Return true if scaling is done around the focus point of the pinch.
     *
     * @return boolean
     */
    public boolean supportsScaleAtFocusPoint () {
        return true;
    }



    /**
     * Return true if pinch zooming is supported.
     *
     * @return boolean
     */
    public boolean supportsZoom () {
        return true;
    }



// Class definitions

    /**
     * ScaleListener
     *
     * Class to see if there is a scale movement performed by the user it overwrites the functions of the initaial class
     * to convert positions and get the scaling and compute minimum and maximum of scalling.
     */
    protected class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {



        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (!mSupportsZoom) return true;
            //store old sclale factor

            float oldScaleFator = mScaleFactor;
            mScaleFactor *= (detector.getScaleFactor()*detector.getScaleFactor());

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            mFocusX = detector.getFocusX ();
            mFocusY = detector.getFocusY ();

            //distance between focus and old origin
            float dx = mFocusX-mPosX;
            float dy = mFocusY-mPosY;
            //distance between focus and new origin after rescale
            float dxSc = dx * mScaleFactor / oldScaleFator;
            float dySc = dy * mScaleFactor / oldScaleFator;

            // calcul of the new origin
            mPosX = mFocusX - dxSc;
            mPosY = mFocusY - dySc;

            invalidate();
            return true;
        }




    }

}
