package com.dwarrow.calendar

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import android.content.Context
import kotlin.math.*

/** Decorative artwork; the moon illustration is deliberately not astronomical data. */
class KhazadMoon:Drawable(){
 private val p=Paint(3)
 override fun draw(c:Canvas){
  val b=bounds;val cx=b.exactCenterX();val cy=b.exactCenterY();val r=min(b.width(),b.height())*.43f
  if(r<=0f)return
  p.style=Paint.Style.FILL
  p.shader=RadialGradient(cx+r*.24f,cy-r*.18f,r*1.6f,intArrayOf(0xffd6d4cb.toInt(),0xff777a7a.toInt(),0xff292e35.toInt(),0xff090d13.toInt()),floatArrayOf(0f,.36f,.72f,1f),Shader.TileMode.CLAMP)
  c.drawCircle(cx,cy,r,p);p.shader=null
  c.save();val clip=Path().apply{addCircle(cx,cy,r,Path.Direction.CW)};c.clipPath(clip)
  for(i in 0..64){
   val a=i*2.39996f;val rr=sqrt((i+.5f)/65f)*r
   val x=cx+cos(a)*rr;val y=cy+sin(a)*rr
   val sz=r*(.018f+(i%7)*.008f)
   p.color=if(i%3==0)0x35464b51 else 0x246e7476
   c.drawCircle(x,y,sz,p)
  }
  p.shader=LinearGradient(cx-r,cy,cx+r,cy,intArrayOf(0xb9090d12.toInt(),0x14000000,0x00000000),null,Shader.TileMode.CLAMP)
  c.drawRect(cx-r,cy-r,cx+r,cy+r,p);p.shader=null
  c.restore()
  p.color=0x66d7b27b;p.style=Paint.Style.STROKE;p.strokeWidth=1.5f;c.drawCircle(cx,cy,r,p);p.style=Paint.Style.FILL
 }
 override fun setAlpha(alpha:Int){p.alpha=alpha}
 override fun setColorFilter(cf:ColorFilter?){p.colorFilter=cf}
 @Deprecated("Deprecated in Java") override fun getOpacity()=PixelFormat.TRANSLUCENT
}

class KhazadNavIcon(context:Context,private val kind:Int,private val selected:Boolean):View(context){
 private val p=Paint(3)
 override fun onDraw(c:Canvas){
  super.onDraw(c)
  val w=width.toFloat();val h=height.toFloat();val s=min(w,h)*.28f
  c.save();c.translate(w/2,h/2);c.scale(s/30f,s/30f)
  p.style=Paint.Style.STROKE;p.strokeWidth=3.1f;p.strokeJoin=Paint.Join.ROUND;p.strokeCap=Paint.Cap.ROUND
  p.color=if(selected)0xfff1b66d.toInt() else 0xffaa7745.toInt()
  fun line(vararg coords:Float){val path=Path();path.moveTo(coords[0],coords[1]);var i=2;while(i<coords.size){path.lineTo(coords[i],coords[i+1]);i+=2};c.drawPath(path,p)}
  when(kind){
   0->{line(-19f,4f,0f,-19f,19f,4f);line(-12f,1f,-12f,20f,12f,20f,12f,1f);line(-4f,20f,-4f,5f,4f,5f,4f,20f);line(-25f,3f,0f,-25f,25f,3f)}
   1->{c.drawRoundRect(-20f,-18f,20f,21f,3f,3f,p);line(-20f,-7f,20f,-7f);for(x in -1..1)for(y in 0..2)c.drawRect(x*12f-3,y*9f-1,x*12f+3,y*9f+5,p);line(-11f,-24f,-11f,-12f);line(11f,-24f,11f,-12f)}
   2->{p.style=Paint.Style.FILL;c.drawCircle(0f,0f,21f,p);p.color=0xff11151a.toInt();c.drawCircle(10f,-7f,19f,p)}
   3->{line(0f,-24f,7f,-7f,24f,0f,7f,7f,0f,24f,-7f,7f,-24f,0f,-7f,-7f,0f,-24f);line(-14f,-14f,14f,14f);line(-14f,14f,14f,-14f)}
   4->{p.style=Paint.Style.FILL;for(x in -1..1){c.save();c.translate(x*15f,0f);c.rotate(45f);c.drawRect(-5f,-5f,5f,5f,p);c.restore()}}
  }
  c.restore()
 }
}
