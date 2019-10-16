package com.kangjj.opengl.es.filters;

import android.content.Context;

import com.kangjj.opengl.es.R;

public class CameraFilter extends BaseFilter {
    public CameraFilter(Context context) {
        super(context, R.raw.camera_vertex, R.raw.camera_fragment);
    }
}
