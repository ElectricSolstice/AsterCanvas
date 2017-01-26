package astercanvas.astercanvas;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CanvasView extends SurfaceView implements View.OnTouchListener, SurfaceHolder.Callback {
    private void init() {
        paint = new Paint(Paint.DITHER_FLAG);
        paint.setARGB(255, 0, 0, 0);
        paint.setStrokeWidth(5.0f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        holder = getHolder();
        holder.addCallback(this);
        drawToBitmap = null;
        drawPath = new Path();
        bitmap = null;
        settingBitmap = false;
        setFocusable(true);
    }

    public CanvasView(Context context) {
        super(context);
        init();
    }

    public CanvasView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        init();
    }

    public void setImageBitmap(Bitmap bmp) {
        bitmap = bmp;
        drawToBitmap = new Canvas(bitmap);
        if (holderReady) {
            Canvas c = holder.lockCanvas();
            onDraw(c);
            holder.unlockCanvasAndPost(c);
        } else {
            settingBitmap = true;
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
        Canvas canvas;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getHistorySize() > 0) {
                    /*prevOnTouchX = event.getHistoricalX(0,0);
                    prevOnTouchY = event.getHistoricalY(0,0);*/
                    drawPath.moveTo(event.getHistoricalX(0,0),event.getHistoricalY(0,0));
                } else {
                    /*prevOnTouchX = event.getX();
                    prevOnTouchY = event.getY();*/
                    drawPath.moveTo(event.getX(),event.getY());
                }
                //drawPath.moveTo(prevOnTouchX,prevOnTouchY);
            case MotionEvent.ACTION_MOVE:
                final int history = event.getHistorySize();
                final int pointers = event.getPointerCount();

                //gather points
                // *DIMENSIONS because every point has that many values (x,y)
                // +1 for the current point (getX,getY)
                float[] pts = new float [(history*pointers+1)*DIMENSIONS];
                for (int h=0;h<history;++h) {
                    for (int p=0;p<pointers;++p) {
                        pts[h*pointers*DIMENSIONS+DIMENSIONS*p] = event.getHistoricalX(p,h);
                        pts[h*pointers*DIMENSIONS+DIMENSIONS*p+1] = event.getHistoricalY(p,h);
                    }
                }
                pts[DIMENSIONS*history*pointers]=event.getX();
                pts[DIMENSIONS*history*pointers+1]=event.getY();;

                for (int i=0;i<pts.length;i+=DIMENSIONS) {
                    drawPath.lineTo(pts[i],pts[i+1]);
                }

                //display what was drawn
                canvas = holder.lockCanvas();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                return true;
            case MotionEvent.ACTION_UP:
                drawToBitmap.drawPath(drawPath,paint);
                drawPath.reset();
                //display what was drawn
                canvas = holder.lockCanvas();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                return true;
        }
        return false;
    }

    public void clear () {
        initBitmap(width,height);
        Canvas c = holder.lockCanvas();
        onDraw(c);
        holder.unlockCanvasAndPost(c);
    }

    public int save (Bitmap.CompressFormat format, FileDescriptor fileDesc) {
        FileOutputStream stream;
        try {
            Context context = getContext();
            stream = new FileOutputStream(fileDesc);
            bitmap.compress(format, 80, stream);
            stream.close();
            //prevent reset of bitmap
            settingBitmap=true;
        } catch (FileNotFoundException e) {
            //TODO
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    public int load (FileDescriptor fileDesc) {;
        try {
            Context context = getContext();
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inMutable = true;
            bitmap.recycle();
            setImageBitmap(BitmapFactory.decodeFileDescriptor(fileDesc, null, bitmapOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    protected void initBitmap(int w, int h) {
        bitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        int[] pixels = new int [w*h];
        for (int i=0;i<w*h;++i) {
            pixels[i] = 0xff777777;
        }
        bitmap.setPixels(pixels,0,w,0,0,w,h);
        drawToBitmap = new Canvas(bitmap);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int newWidth, int newHeight) {
        width = newWidth;
        height = newHeight;
        if (!settingBitmap) {
            initBitmap(width, height);
            settingBitmap=false;
        }
        holderReady=true;
        Canvas c = holder.lockCanvas();
        onDraw(c);
        holder.unlockCanvasAndPost(c);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        width = getWidth();
        height = getHeight();
        if (!settingBitmap) {
            initBitmap(width, height);
            settingBitmap=false;
        }
        holderReady=true;
        Canvas c = holder.lockCanvas();
        onDraw(c);
        holder.unlockCanvasAndPost(c);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        holderReady = false;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(Color.DKGRAY);
        canvas.drawBitmap(bitmap,new Matrix(),paint);
        if (!drawPath.isEmpty()) {
            canvas.drawPath(drawPath,paint);
        }
    }

    int getBrushColor () {
        return paint.getColor();
    }

    void setBrushColor (int color) {
        paint.setColor(color);
    }

    float getBrushWidth () { return paint.getStrokeWidth(); }

    void setBrushWidth (float width) { paint.setStrokeWidth(width); }

    public String[] getPermissionsRequired () {
        return PERMISSIONS_REQUIRED;
    }

    public final int DIMENSIONS=2;
    int width;
    int height;
    Paint paint;
    SurfaceHolder holder;
    Canvas drawToBitmap;
    Path drawPath;
    Bitmap bitmap;
    boolean holderReady;
    boolean settingBitmap;
    private static final String[] PERMISSIONS_REQUIRED = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
}
