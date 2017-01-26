package astercanvas.astercanvas;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class BrushSizeDialog extends DialogFragment {

    public void setText (String val) {
        if (editable != null) {
            editable.setText(val);
        } else {
            editText=val;
        }
    }

    public void setInputType(int type) {
        editable.setInputType(type);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View view = inflater.inflate(R.layout.brush_size, container, false);
        editable = (EditText) view.findViewById(R.id.editText);
        if (editText != null) {
            editable.setText(editText);
        }
        ((Button)view.findViewById(R.id.okButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvas.setBrushWidth(Float.parseFloat(getText()));
                dismiss();
            }
        });
        ((Button)view.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    public String getText () {
        return editable.getText().toString();
    }

    public void setCanvasView (CanvasView canvasView) {
        canvas = canvasView;
    }

    EditText editable;
    CanvasView canvas;
    String editText;
}
