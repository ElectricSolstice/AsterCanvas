package astercanvas.astercanvas;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;

import yuku.ambilwarna.AmbilWarnaDialog;


public class AsterCanvasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
        img = (CanvasView) findViewById(R.id.canvasView);
        img.setOnTouchListener(img);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options,menu);

        Drawable icon = menu.findItem(R.id.colorOption).getIcon();
        icon.mutate();
        icon.setColorFilter(img.getBrushColor(), PorterDuff.Mode.SRC_ATOP);
        icon.invalidateSelf();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        Drawable icon = menu.findItem(R.id.colorOption).getIcon();
        icon.mutate();
        icon.setColorFilter(img.getBrushColor(), PorterDuff.Mode.SRC_ATOP);
        icon.invalidateSelf();
        return super.onPrepareOptionsMenu(menu);
    }

    public final static int REQUEST_SAVE_CODE = 1;
    public final static int REQUEST_LOAD_CODE = 2;
    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newOption:
                img.clear();
                break;
            case R.id.saveAsOption:
                Intent save = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                save.addCategory(Intent.CATEGORY_OPENABLE);
                save.setType("image/*");
                save.putExtra(Intent.EXTRA_TITLE,"fileName.png");
                try {
                    startActivityForResult(save,REQUEST_SAVE_CODE);
                } catch (ActivityNotFoundException e) {
                    //TODO: make an error dialog pop up
                } catch (Exception e) {
                }
                break;
            case R.id.loadOption:
                Intent load = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                load.addCategory(Intent.CATEGORY_OPENABLE);
                load.setType("image/*");
                try {
                    startActivityForResult(load,REQUEST_LOAD_CODE);
                } catch (ActivityNotFoundException e) {
                    //TODO: make an error dialog pop up
                } catch (Exception e) {
                }
                break;
            case R.id.colorOption:
                int initialColor = img.getBrushColor();
                AmbilWarnaDialog colorDialog = new AmbilWarnaDialog(this, initialColor, false,
                        new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        AsterCanvasActivity.this.img.setBrushColor(color);
                        AsterCanvasActivity.this.invalidateOptionsMenu();
                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                    }
                });
                colorDialog.show();
                break;
            case R.id.brushSizeOption:
                BrushSizeDialog sizeDialog = new BrushSizeDialog();
                sizeDialog.setCanvasView(img);
                sizeDialog.setText(Float.toString(img.getBrushWidth()));
                sizeDialog.show(getSupportFragmentManager(),"brushSize");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data==null) {
            //TODO show error dialog
            return;
        }
        FileDescriptor fileDesc = null;
        int status;
        switch (requestCode) {
            case REQUEST_SAVE_CODE:
                try {
                    fileDesc = getContentResolver().openFileDescriptor(data.getData(),"w").getFileDescriptor();
                } catch (FileNotFoundException e) {
                    //TODO
                    return;
                }
                status = img.save(Bitmap.CompressFormat.PNG,fileDesc);
                if (status != 0) {
                    //TODO
                }
                break;
            case REQUEST_LOAD_CODE:
                try {
                    fileDesc = getContentResolver().openFileDescriptor(data.getData(),"r").getFileDescriptor();
                } catch (FileNotFoundException e) {
                    //TODO
                    return;
                }
                status = img.load(fileDesc);
                if (status != 0) {
                    //TODO
                }
                break;
        }
    }

    CanvasView img;
}
