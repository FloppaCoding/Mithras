package floppacoding.aurora.mc_modern;

import floppacoding.aurora.core.FrameBuffer;
import floppacoding.aurora.core.FrameBufferReference;
import floppacoding.mithras.Mithras;

public class test {
    FrameBuffer buffer = new FrameBufferReference(1, Mithras.mc.getWindow()::getWidth, Mithras.mc.getWindow()::getHeight);
}
