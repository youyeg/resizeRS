package com.yousef.imageresizer;

import android.graphics.Bitmap;
import android.support.v8.renderscript.*;


public class imageopsRS {
    public static Bitmap bmpresize(RenderScript rs, Bitmap src, int destW, int destH) {
        Bitmap.Config bmpCfg = src.getConfig();
        Allocation tmp = Allocation.createFromBitmap(rs, src);
        Allocation tmpType = Allocation.createTyped(rs, tmp.getType());
        tmp.destroy();
        Bitmap dst = Bitmap.createBitmap(destW, destH, bmpCfg);
        Type t = Type.createXY(rs, tmpType.getElement(), destW, destH);
        Allocation tmpO = Allocation.createTyped(rs, t);
        ScriptIntrinsicResize rsIntr = ScriptIntrinsicResize.create(rs);
        rsIntr.setInput(tmpType);
        rsIntr.forEach_bicubic(tmpO);
        tmpO.copyTo(dst);
        tmpType.destroy();
        tmpO.destroy();
        rsIntr.destroy();

        return dst;
    }


    public static Bitmap YUV2RGB(RenderScript rs,byte[] yuvByteArray,int W,int H) {
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(yuvByteArray.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(W).setY(H);
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        in.copyFrom(yuvByteArray);
        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        Bitmap bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        out.copyTo(bmp);
        yuvToRgbIntrinsic.destroy();
        rs.destroy();
        return bmp;
    }

    public static Bitmap resizeBitmap2(RenderScript rs, Bitmap src, int dstWidth) {
        Bitmap.Config  bitmapConfig = src.getConfig();
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        float srcAspectRatio = (float) srcWidth / srcHeight;
        int dstHeight = (int) (dstWidth / srcAspectRatio);

        float resizeRatio = (float) srcWidth / dstWidth;

        /* Calculate gaussian's radius */
        float sigma = resizeRatio / (float) Math.PI;
        // https://android.googlesource.com/platform/frameworks/rs/+/master/cpu_ref/rsCpuIntrinsicBlur.cpp
        float radius = 2.5f * sigma - 1.5f;
        radius = Math.min(25, Math.max(0.0001f, radius));

        /* Gaussian filter */
        Allocation tmpIn = Allocation.createFromBitmap(rs, src);
        Allocation tmpFiltered = Allocation.createTyped(rs, tmpIn.getType());
        ScriptIntrinsicBlur blurInstrinsic = ScriptIntrinsicBlur.create(rs, tmpIn.getElement());

        blurInstrinsic.setRadius(radius);
        blurInstrinsic.setInput(tmpIn);
        blurInstrinsic.forEach(tmpFiltered);

        tmpIn.destroy();
        blurInstrinsic.destroy();

        /* Resize */
        Bitmap dst = Bitmap.createBitmap(dstWidth, dstHeight, bitmapConfig);
        Type t = Type.createXY(rs, tmpFiltered.getElement(), dstWidth, dstHeight);
        Allocation tmpOut = Allocation.createTyped(rs, t);
        ScriptIntrinsicResize resizeIntrinsic = ScriptIntrinsicResize.create(rs);

        resizeIntrinsic.setInput(tmpFiltered);
        resizeIntrinsic.forEach_bicubic(tmpOut);
        tmpOut.copyTo(dst);

        tmpFiltered.destroy();
        tmpOut.destroy();
        resizeIntrinsic.destroy();

        return dst;
    }
}