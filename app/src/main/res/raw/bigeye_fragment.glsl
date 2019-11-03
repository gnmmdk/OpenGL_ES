precision mediump float;

varying vec2 aCoord;
//采样器
uniform sampler2D vTexture;
uniform vec2 left_eye;//左眼坐标
uniform vec2 right_eye;

//用代码翻译算法公式
//r：原来的点距离眼睛中心点的距离
//rmax 放大区域的半径
float fs(float r,float rmax){
    flaot = a = 0.4;//放大系数
    return (1.0 - pow(r / rmax - 1.0,2.0)*a)*r;//pow 内置函数 x的几次方(r / rmax - 1.0）的2次方
}

//在放大区域内，找眼睛中的某个点的像素来替代（复制眼睛里的像素到要放大的区域）
//计算新的点
//coord:原来的点
//eye：眼睛坐标
//rmax:放大区域半径
vec2 calcNewCoord(vec2 oldCoord,vec2 eye,float rmax){
    vec2 newCoord = oldCoord;
    float r= distance(oldCoord,eye);
    //在区域方位内才进行放大处理
    if(r >0.0f && r < rmax){
        float fsr = fs(r,rmax);
        //      （新的点 - 眼睛） /  （旧的点 - 眼睛） = 新的距离 / 旧的距离
        newCoord = (fsr / r) * (oldCoord - eye) + eye;
    }
    return newCoord;
}

void main(){
    //根据两眼间距获取放大区域的半径 rmax
    flaot rmax = distance(left_eye,rigth_eye)/2.0;
    vec2 newCoord = calcNewCoord(aCoord,left_eye,rmax);
    newCoord =calcNewCoord(newCoord,right_eye,rmax);
    gl_FragColor= texture2D(vTexture,newCoord);
}