//float数据是什么精度的
precision mediump float;
//采样点的坐标
varying vec2 aCoord;
//采样器
uniform sampler2D vTexture;
void main() {
    //变量 接收像素值
    // textture2D:采样器 采集aCoord的像素
    // 赋值给gl_FragColor就可以了
    gl_FragColor = texture2D(vTexture,aCoord);
}
