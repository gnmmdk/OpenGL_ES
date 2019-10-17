//把顶点坐标给这个变量，确定要画画的形状
//attrobite 属性变量。只能用于顶点着色器中。一般用于该变量来表示
//一些顶点数据，如：顶点坐标、纹理坐标、颜色等。
//vec4含四个浮点型数据的向量(xyzw,rgba,stpq)
attribute vec4 vPosition;
//接收纹理坐标，接收采样器图片的坐标
attribute vec4 vCoord;
//变换矩阵，需要将原本的vCoord(01,11,00,10)与矩阵相乘，
//才能够得到surfaceTexure（特殊）的正确的采样坐标
//uniform 一致变量。在着色器执行期间一致变量的值是不变的。与const常量
//不同的是，这个值在编译时期是未知的，是由着色器外部初始化的。
uniform mat4 vMatrix;
//传给片元着色器 像素点
//varying易变变量。是从顶点着色器传递到片元着色器的数据变量
varying vec2 aCoord;

void main(){
    //gl_Position vec4类型，表示顶点着色器中顶点位置
    //内置变量 gl_Position ,我们把顶点数据赋值给这个变量 opengl就知道它要画什么形状了
    gl_Position = vPosition;
    //和设备有关
//    aCoord = (vMatrix * vCoord).xy;
    aCoord = vCoord.xy;
//    aCoord =  vec2((vCoord*vMatrix).x,(vCoord*vMatrix).y);
}