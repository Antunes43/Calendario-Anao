package com.dwarrow.calendar

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.location.Location
import android.os.Bundle
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.BitmapDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import java.time.LocalDate
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.json.JSONArray

private class ForgedFrame(private val mode:Int=0):Drawable(){
 private val p=Paint(3)
 override fun draw(c:Canvas){
  val b=bounds;val w=b.width().toFloat();val h=b.height().toFloat()
  if(w<=0f||h<=0f)return
  val dark=if(mode==2)0xffe7dcca.toInt() else 0xff090c10.toInt()
  p.shader=LinearGradient(0f,0f,w,h,intArrayOf(if(mode==2)0xfff9f0df.toInt() else 0xff30353b.toInt(),dark,if(mode==2)0xffd9c7a9.toInt() else 0xff16191d.toInt()),null,Shader.TileMode.CLAMP)
  c.drawRect(0f,0f,w,h,p);p.shader=null
  p.color=if(mode==2)0x1c806040 else 0x305d6265
  for(i in 0..110){val yy=((i*73)%997)/997f*h;val xx=((i*173)%991)/991f*w;c.drawLine(xx,yy,(xx+25f).coerceAtMost(w),(yy-3f).coerceAtLeast(0f),p)}
  fun border(inset:Float,color:Int,sw:Float){
   p.color=color;p.style=Paint.Style.STROKE;p.strokeWidth=sw
   c.drawRect(inset,inset,w-inset,h-inset,p);p.style=Paint.Style.FILL
  }
  if(mode!=2){
   p.shader=LinearGradient(0f,0f,22f,0f,intArrayOf(0xff12171c.toInt(),0xff80542e.toInt(),0xff222831.toInt(),0xff080a0d.toInt()),null,Shader.TileMode.CLAMP)
   c.drawRect(0f,0f,17f,h,p);c.drawRect(w-17f,0f,w,h,p);p.shader=null
   p.color=0xffb87c41.toInt();p.style=Paint.Style.STROKE;p.strokeWidth=2f
   for(y in 35..(h.toInt()-35) step 58){
    val d=Path().apply{moveTo(8f,y-10f);lineTo(14f,y.toFloat());lineTo(8f,y+10f);lineTo(2f,y.toFloat());close()}
    c.drawPath(d,p)
    c.save();c.translate(w-16f,0f);c.drawPath(d,p);c.restore()
   }
   p.style=Paint.Style.FILL
  }
  val gold=if(mode==1)0xffd2a15a.toInt() else 0xffa87743.toInt()
  border(2f,0xff493c32.toInt(),6f);border(5f,gold,2f);border(9f,0xff3e3227.toInt(),3f);border(13f,0xff785637.toInt(),1f)
  p.color=gold
  val mid=w/2f
  for(y in floatArrayOf(6f,h-6f)){val q=Path().apply{moveTo(mid,y-8f);lineTo(mid+10f,y);lineTo(mid,y+8f);lineTo(mid-10f,y);close()};c.drawPath(q,p)}
  val r=13f
  p.color=0xffd3a365.toInt()
  for(x in floatArrayOf(r,w-r))for(y in floatArrayOf(r,h-r)){
   val path=Path().apply{moveTo(x,y-5);lineTo(x+5,y);lineTo(x,y+5);lineTo(x-5,y);close()}
   c.drawPath(path,p)
  }
  p.color=0x19bd8b55
  for(i in 0..8){val y=24f+i*(h-48f)/9f;c.drawLine(20f,y,w-20f,y+3f,p)}
 }
 override fun setAlpha(a:Int){p.alpha=a}
 override fun setColorFilter(cf:android.graphics.ColorFilter?){p.colorFilter=cf}
 @Deprecated("Deprecated in Java") override fun getOpacity():Int=PixelFormat.OPAQUE
}

// Preserve the original artwork aspect ratio rather than stretching ornaments.
// Nine-slice panel: corners retain their geometry; only plain edge/center strips resize.
// Keep ornate corners and border strips intact while filling a variable-height panel.
private class ProportionalArt(private val bitmap:Bitmap):Drawable(){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 override fun draw(c:Canvas){
  val b=bounds
  if(b.isEmpty)return
  val w=b.width().toFloat();val h=b.height().toFloat()
  val sw=bitmap.width;val sh=bitmap.height
  val edgeX=(sw*0.12f).toInt().coerceAtLeast(1)
  val edgeY=(sh*0.22f).toInt().coerceAtLeast(1)
  val dstX=kotlin.math.min(w*0.12f,edgeX.toFloat())
  val dstY=kotlin.math.min(h*0.22f,edgeY.toFloat())
  val sx=intArrayOf(0,edgeX,sw-edgeX,sw)
  val sy=intArrayOf(0,edgeY,sh-edgeY,sh)
  val dx=floatArrayOf(b.left.toFloat(),b.left+dstX,b.right-dstX,b.right.toFloat())
  val dy=floatArrayOf(b.top.toFloat(),b.top+dstY,b.bottom-dstY,b.bottom.toFloat())
  for(y in 0..2)for(x in 0..2)
   c.drawBitmap(bitmap,Rect(sx[x],sy[y],sx[x+1],sy[y+1]),RectF(dx[x],dy[y],dx[x+1],dy[y+1]),p)
 }
 override fun setAlpha(a:Int){p.alpha=a}
 override fun setColorFilter(f:android.graphics.ColorFilter?){p.colorFilter=f}
 @Deprecated("Deprecated in Java") override fun getOpacity():Int=PixelFormat.OPAQUE
}
// Lunar art is composed from independent, aspect-safe pieces: stone, borders, corners and central ring.
private class AdaptiveLunarFrame(private val art:Bitmap):Drawable(){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 override fun draw(c:Canvas){
  val b=bounds
  if(b.isEmpty)return
  val w=b.width().toFloat();val h=b.height().toFloat()
  val sw=art.width.toFloat();val sh=art.height.toFloat()
  fun patch(x0:Float,y0:Float,x1:Float,y1:Float,dx0:Float,dy0:Float,dx1:Float,dy1:Float){
   c.drawBitmap(art,Rect(x0.toInt(),y0.toInt(),x1.toInt(),y1.toInt()),RectF(dx0,dy0,dx1,dy1),p)
  }
  // A single stone surface, rather than independently stretched blocks with visible seams.
  p.color=0xff101318.toInt();c.drawRect(b,p)
  p.alpha=140
  patch(sw*.09f,sh*.45f,sw*.29f,sh*.84f,b.left.toFloat(),b.top.toFloat(),b.right.toFloat(),b.bottom.toFloat())
  p.alpha=255
  p.shader=LinearGradient(b.left.toFloat(),b.top.toFloat(),b.right.toFloat(),b.bottom.toFloat(),
   intArrayOf(0x66101318,0x22090b0e,0x77101318),null,Shader.TileMode.CLAMP)
  c.drawRect(b,p);p.shader=null
  // Continuous forged perimeter, drawn at the actual panel size.
  p.style=Paint.Style.STROKE
  p.color=0xffa87847.toInt();p.strokeWidth=2.2f
  c.drawRect(b.left+3f,b.top+3f,b.right-3f,b.bottom-3f,p)
  p.color=0xff40372c.toInt();p.strokeWidth=1.5f
  c.drawRect(b.left+7f,b.top+7f,b.right-7f,b.bottom-7f,p)
  p.style=Paint.Style.FILL
  // Original corner ornaments are preserved at their own aspect ratio.
  val srcSize=sh*.17f
  val dstSize=kotlin.math.min(kotlin.math.min(h*.19f,w*.065f),32f)
  for(right in 0..1)for(bottom in 0..1){
   val sx=if(right==0)0f else sw-srcSize
   val sy=if(bottom==0)0f else sh-srcSize
   val dx=if(right==0)b.left.toFloat() else b.right-dstSize
   val dy=if(bottom==0)b.top.toFloat() else b.bottom-dstSize
   patch(sx,sy,sx+srcSize,sy+srcSize,dx,dy,dx+dstSize,dy+dstSize)
  }

 }
 override fun setAlpha(a:Int){p.alpha=a}
 override fun setColorFilter(f:android.graphics.ColorFilter?){p.colorFilter=f}
 @Deprecated("Deprecated in Java") override fun getOpacity():Int=PixelFormat.OPAQUE
}
// Independent ornamental ring: no sampled PNG, no background and no image stretching.
private class ForgedMoonRing:Drawable(){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 override fun draw(c:Canvas){
  val b=bounds;val cx=b.exactCenterX();val cy=b.exactCenterY()
  val r=kotlin.math.min(b.width(),b.height())*.455f
  if(r<=0f)return
  val bands=listOf(Triple(0f,7f,0xff171719.toInt()),Triple(3f,4f,0xffb7844c.toInt()),
   Triple(8f,3f,0xff393a3c.toInt()),Triple(12f,2f,0xffd5aa6d.toInt()),
   Triple(16f,3f,0xff49301e.toInt()))
  p.style=Paint.Style.STROKE
  for((offset,stroke,color) in bands){p.strokeWidth=stroke;p.color=color;c.drawCircle(cx,cy,r-offset,p)}
  // Forged radial scoring around the outer metal band.
  for(i in 0 until 48){
   val angle=Math.PI*2*i/48.0
   val x1=cx+kotlin.math.cos(angle).toFloat()*(r-5f)
   val y1=cy+kotlin.math.sin(angle).toFloat()*(r-5f)
   val x2=cx+kotlin.math.cos(angle).toFloat()*(r-10f)
   val y2=cy+kotlin.math.sin(angle).toFloat()*(r-10f)
   p.color=if(i%4==0)0xffc69b60.toInt() else 0xff584633.toInt()
   p.strokeWidth=if(i%4==0)1.8f else .9f
   c.drawLine(x1,y1,x2,y2,p)
  }
  for(i in 0..3){
   val angle=Math.PI*i/2.0-Math.PI/2
   val x=cx+kotlin.math.cos(angle).toFloat()*r
   val y=cy+kotlin.math.sin(angle).toFloat()*r
   val d=r*.15f
   val diamond=Path().apply{moveTo(x,y-d);lineTo(x+d*.78f,y);lineTo(x,y+d);lineTo(x-d*.78f,y);close()}
   p.style=Paint.Style.FILL;p.color=0xff141719.toInt();c.drawPath(diamond,p)
   p.style=Paint.Style.STROKE;p.strokeWidth=3f;p.color=0xffa57443.toInt();c.drawPath(diamond,p)
   val inner=Path().apply{moveTo(x,y-d*.52f);lineTo(x+d*.40f,y);lineTo(x,y+d*.52f);lineTo(x-d*.40f,y);close()}
   p.style=Paint.Style.FILL;p.color=0xff714b2c.toInt();c.drawPath(inner,p)
   p.style=Paint.Style.STROKE;p.strokeWidth=1.2f;p.color=0xffe0b278.toInt();c.drawPath(inner,p)
  }
  p.style=Paint.Style.FILL
 }
 override fun setAlpha(alpha:Int){p.alpha=alpha}
 override fun setColorFilter(filter:android.graphics.ColorFilter?){p.colorFilter=filter}
 @Deprecated("Deprecated in Java") override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}
// Content-first frame: the bitmap supplies only stone texture; borders are drawn at final view size.
private class AdaptiveStoneFrame(private val stone:Bitmap?):Drawable(){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val dark=0xff0b0d11.toInt()
 override fun draw(c:Canvas){
  val b=bounds
  if(b.isEmpty)return
  val w=b.width().toFloat();val h=b.height().toFloat()
  p.color=dark;c.drawRect(b,p)
  if(stone!=null){
   val sw=stone.width;val sh=stone.height
   // Sample the undecorated central stone, never the source border or side emblems.
   val src=Rect((sw*.36f).toInt(),(sh*.35f).toInt(),(sw*.64f).toInt(),(sh*.65f).toInt())
   p.alpha=195
   c.drawBitmap(stone,src,RectF(b.left+4f,b.top+4f,b.right-4f,b.bottom-4f),p)
   p.alpha=255
  }
  p.style=Paint.Style.STROKE
  p.strokeWidth=2f;p.color=0xffaa7543.toInt()
  c.drawRoundRect(RectF(b.left+3f,b.top+3f,b.right-3f,b.bottom-3f),2f,2f,p)
  p.strokeWidth=1f;p.color=0xff59432e.toInt()
  c.drawRect(b.left+7f,b.top+7f,b.right-7f,b.bottom-7f,p)
  p.style=Paint.Style.FILL
  p.color=0xffb58a51.toInt()
  val r=kotlin.math.min(8f,kotlin.math.min(w,h)*.08f)
  for(x in floatArrayOf(b.left+7f,b.right-7f))for(y in floatArrayOf(b.top+7f,b.bottom-7f)){
   val gem=Path().apply{moveTo(x,y-r);lineTo(x+r,y);lineTo(x,y+r);lineTo(x-r,y);close()}
   c.drawPath(gem,p)
  }
 }
 override fun setAlpha(a:Int){p.alpha=a}
 override fun setColorFilter(f:android.graphics.ColorFilter?){p.colorFilter=f}
 @Deprecated("Deprecated in Java") override fun getOpacity():Int=PixelFormat.OPAQUE
}
class MainActivity:Activity(){
 private val names=arrayOf("Khazad-dûm","Dornogal","Dwarrow Scholar")
 private val bronze=0xffd8a35f.toInt()
 private val cream=0xfff3e8d7.toInt()
 private var themeIndex=0
 private var tab=0
 private val locationRequestCode=740
 private fun locationAllowed():Boolean =
  checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED ||
  checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED
 private fun savedLocation():String {
  val prefs=getPreferences(MODE_PRIVATE)
  if(!prefs.contains("durin_lat"))return "Nenhuma localização disponível"
  return String.format(Locale("pt","BR"),"%.3f°, %.3f°",prefs.getFloat("durin_lat",0f),prefs.getFloat("durin_lon",0f))
 }
 private fun refreshDurinLocation(){
  if(!locationAllowed()){
   requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),locationRequestCode)
   return
  }
  try{
   val manager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
   val available=listOf(LocationManager.GPS_PROVIDER,LocationManager.NETWORK_PROVIDER)
    .filter{manager.isProviderEnabled(it)}
   val last=available.mapNotNull{manager.getLastKnownLocation(it)}.maxByOrNull{it.time}
   if(last!=null){
    getPreferences(MODE_PRIVATE).edit().putFloat("durin_lat",last.latitude.toFloat())
     .putFloat("durin_lon",last.longitude.toFloat()).apply()
    Toast.makeText(this,"Localização atualizada",Toast.LENGTH_SHORT).show()
   }else Toast.makeText(this,"Ainda não há uma localização conhecida no aparelho",Toast.LENGTH_LONG).show()
  }catch(e:SecurityException){
   Toast.makeText(this,"Permissão de localização necessária",Toast.LENGTH_SHORT).show()
  }
  draw()
 }
 override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,grantResults:IntArray){
  super.onRequestPermissionsResult(requestCode,permissions,grantResults)
  if(requestCode==locationRequestCode&&locationAllowed())refreshDurinLocation()
 }

 private var selectedEventId:String?=null
 private var date=LocalDate.now()
 private val lunarImages=arrayOf("lua_nova","lua_crescente","quarto_crescente","gibosa_crescente","lua_cheia","gibosa_minguante","quarto_minguante","lua_minguante")
 private val lunarNames=arrayOf("Lua nova","Lua crescente","Quarto crescente","Gibosa crescente","Lua cheia","Gibosa minguante","Quarto minguante","Lua minguante")
 // Approximate synodic cycle anchored at the 2000-01-06 18:14 UTC new moon.
 // Intended as an indicative phase, not a precise astronomical ephemeris.
 private fun lunarPhase(day:LocalDate):Triple<Int,Int,Double>{
  val epoch=Instant.parse("2000-01-06T18:14:00Z")
  val noon=day.atTime(12,0).atZone(ZoneId.systemDefault()).toInstant()
  val elapsed=ChronoUnit.SECONDS.between(epoch,noon)/86400.0
  val cycle=29.530588853
  val age=((elapsed%cycle)+cycle)%cycle
  val position=age/cycle*8.0
  val index=kotlin.math.floor(position+0.5).toInt()%8
  val remaining=((index+0.5)*cycle/8.0-age+cycle)%cycle
  return Triple(index,kotlin.math.ceil(remaining).toInt().coerceAtLeast(1),age)
 }
 private fun seasonArt(name:String):String?=when(name.substringBefore(" · ")){
  "Iklaladrân"->"estacao_iklaladran"
  "Shulukadrân"->"estacao_shulukadran"
  "Kêlithadrân"->"estacao_kelithadran"
  "’Arsadrân"->"estacao_arsadran"
  "Dabladrân"->"estacao_dabladran"
  "Injamadrân"->"estacao_injamadran"
  else->null
 }
 private fun seasonPanel(root:LinearLayout,day:CalendarEngine.Day?){
  panel(root,6,"nurt_events"){p->
   title(p,"ESTAÇÃO ANÃ")
   val season=day?.let{CalendarEngine.season(this,it)} ?: "—"
   val row=horizontal().apply{gravity=Gravity.CENTER;setPadding(0,0,0,0)}
   p.addView(row,LinearLayout.LayoutParams(-1,-2))
   val iconName=seasonArt(season)
   val id=iconName?.let{resources.getIdentifier(it,"drawable",packageName)} ?: 0
   if(id!=0){
    val icon=ImageView(this).apply{setImageResource(id);scaleType=ImageView.ScaleType.FIT_CENTER;contentDescription=season}
    row.addView(icon,LinearLayout.LayoutParams(dp(104),dp(104)).apply{marginEnd=dp(15)})
   }
   val parts=season.split(" · ",limit=2)
   val labels=column().apply{gravity=Gravity.CENTER_VERTICAL}
   row.addView(labels,LinearLayout.LayoutParams(-2,-2).apply{leftMargin=dp(17)})
   add(labels,parts[0],24f,bronze,true)
   if(parts.size>1)add(labels,parts[1],18f,cream)
  }
 }
 private fun weekdayPortuguese(name:String):String=when(name){
  "Izgilnurt"->"segunda-feira"
  "Akhlathnurt"->"terça-feira"
  "Adadnurt"->"quarta-feira"
  "Thanbnurt"->"quinta-feira"
  "Abkân-nurt"->"sexta-feira"
  "Thatrnurt"->"sábado"
  "Ibriznurt"->"domingo"
  else->""
 }
 private fun dp(n:Int)=(n*resources.displayMetrics.density+0.5f).toInt()
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);themeIndex=getPreferences(MODE_PRIVATE).getInt("theme",0).coerceIn(0,2);draw()}
 private fun text(value:String,size:Float=16f,color:Int=cream,bold:Boolean=false):TextView=TextView(this).apply{
  text=value;textSize=size;setTextColor(color);gravity=Gravity.CENTER
  if(bold)setTypeface(null,android.graphics.Typeface.BOLD)
  setPadding(dp(5),dp(3),dp(5),dp(3))
 }
 private fun column():LinearLayout=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL}
 private fun horizontal():LinearLayout=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
 private fun title(root:LinearLayout,heading:String){add(root,"✦  $heading  ✦",13f,bronze,true)}
 private fun artBackground(name:String):Drawable? {
  val id=resources.getIdentifier(name,"drawable",packageName)
  if(id==0)return null
  val bitmap=android.graphics.BitmapFactory.decodeResource(resources,id) ?: return null
  return when(name){
   "nurt_header"->ProportionalArt(bitmap)
   "nurt_lunar"->AdaptiveLunarFrame(bitmap)
   else->AdaptiveStoneFrame(bitmap)
  }
 }
 private fun panel(root:LinearLayout,space:Int=12,art:String?=null,build:(LinearLayout)->Unit){
  val c=column().apply{background=(if(themeIndex==0) art?.let{artBackground(it)} else null) ?: ForgedFrame(themeIndex);setPadding(dp(15),dp(10),dp(15),dp(10))}
  root.addView(c,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(space)})
  build(c)
 }
 private fun add(parent:LinearLayout,value:String,size:Float=16f,color:Int=cream,bold:Boolean=false){parent.addView(text(value,size,color,bold))}
 private fun lunarDivider(parent:LinearLayout){
  val line=View(this).apply{setBackgroundColor(0xff956b40.toInt())}
  parent.addView(line,LinearLayout.LayoutParams(-1,dp(1)).apply{
   leftMargin=dp(7);rightMargin=dp(7);topMargin=dp(3);bottomMargin=dp(4)
  })
 }
 private fun action(label:String,onClick:()->Unit):TextView=text(label,13f,bronze,true).apply{
  background=ForgedFrame(themeIndex);setPadding(dp(8),dp(15),dp(8),dp(15));setOnClickListener{onClick();draw()}
 }

 private val observances by lazy {
  JSONArray(assets.open("observances.json").bufferedReader().use { it.readText() })
 }
 private fun draw(){
  val day=CalendarEngine.lookup(this,date)
  val greg=date.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy",Locale("pt","BR")))
  val bg=if(themeIndex==2)0xffd4c4aa.toInt() else 0xff090b0f.toInt()
  val outer=column().apply{setBackgroundColor(bg)}
  val scroll=ScrollView(this).apply{isFillViewport=false}
  val root=column().apply{setPadding(dp(8),dp(6),dp(8),dp(8))}
  scroll.addView(root)
  outer.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
  if(themeIndex==0){
   val header=FrameLayout(this)
   val id=resources.getIdentifier("nurt_header","drawable",packageName)
   if(id!=0){
    val artwork=ImageView(this).apply{setImageResource(id);scaleType=ImageView.ScaleType.FIT_CENTER}
    header.addView(artwork,FrameLayout.LayoutParams(-1,-1))
   }
   // Place text inside a fixed safe band, between the upper crest and lower trim.
   // Never align it to the bottom of the artwork: font metrics vary by device.
   val heading=column().apply{
    gravity=Gravity.CENTER
    setPadding(dp(12),0,dp(12),0)
   }
   val main=text("NURTFAHANI",17f,bronze,true).apply{includeFontPadding=false;setPadding(0,0,0,0)}
   main.gravity=Gravity.CENTER
   heading.addView(main,LinearLayout.LayoutParams(-1,-2))
   val subtitle=text("CALENDÁRIO ANÃO",10f,cream,true).apply{includeFontPadding=false;setPadding(0,0,0,0)}
   subtitle.gravity=Gravity.CENTER
   heading.addView(subtitle,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3)})
   header.addView(heading,FrameLayout.LayoutParams(-1,dp(42),Gravity.TOP or Gravity.CENTER_HORIZONTAL).apply{topMargin=dp(46)})
   root.addView(header,LinearLayout.LayoutParams(-1,dp(106)).apply{bottomMargin=dp(6)})
  }else{
   panel(root,6){h->
    h.minimumHeight=dp(106)
    add(h,names[themeIndex].uppercase(Locale.ROOT),20f,bronze,true)
    add(h,"CALENDÁRIO ANÃO",10f,cream,true)
   }
  }
  if(tab==0||tab==2){
   panel(root,6,"nurt_lunar"){p->
    val phase=lunarPhase(date)
    val index=phase.first
    val line=horizontal()
    p.addView(line,LinearLayout.LayoutParams(-1,dp(132)))
    val left=column().apply{gravity=Gravity.CENTER}
    line.addView(left,LinearLayout.LayoutParams(0,-2,0.30f))
    add(left,"FASE ATUAL",10f,bronze,true)
    lunarDivider(left)
    add(left,lunarNames[index],14f,cream,true)
    val imageId=resources.getIdentifier(lunarImages[index],"drawable",packageName)
    val moon=ImageView(this).apply{
     if(imageId!=0)setImageResource(imageId) else setImageDrawable(KhazadMoon())
     scaleType=ImageView.ScaleType.FIT_CENTER
     contentDescription=lunarNames[index]
    }
    val lunarStack=FrameLayout(this)
    // The approved ring is a transparent, independent image layered above the moon.
    // FIT_CENTER keeps the forged ornaments square and undistorted.
    lunarStack.addView(moon,FrameLayout.LayoutParams(-1,-1).apply{
     val inset=dp(8);setMargins(inset,inset,inset,inset)
    })
    val ringId=resources.getIdentifier("nurt_lunar_ring_gold","drawable",packageName)
    if(ringId!=0){
     lunarStack.addView(ImageView(this).apply{
      setImageResource(ringId)
      scaleType=ImageView.ScaleType.FIT_CENTER
      contentDescription=null
     },FrameLayout.LayoutParams(-1,-1))
    }else{
     lunarStack.addView(View(this).apply{background=ForgedMoonRing()},FrameLayout.LayoutParams(-1,-1))
    }
    line.addView(lunarStack,LinearLayout.LayoutParams(0,dp(132),0.40f))
    val right=column().apply{gravity=Gravity.CENTER}
    line.addView(right,LinearLayout.LayoutParams(0,-2,0.29f))
    add(right,"PRÓXIMA FASE",10f,bronze,true)
    lunarDivider(right)
    add(right,lunarNames[(index+1)%8],13f,cream,true)
    add(right,"≈ ${phase.second} dia(s)",11f,bronze)

   }
  }
  if(tab==0||tab==1){
   panel(root,6,"nurt_date"){p->
    p.setPadding(dp(14),dp(9),dp(14),dp(9))
    val khuzTitle=text("✦  NURT KHUZDUL  ✦",13f,bronze,true).apply{gravity=Gravity.CENTER}
    p.addView(khuzTitle,LinearLayout.LayoutParams(-1,-2))
    val dateRow=horizontal().apply{gravity=Gravity.CENTER_VERTICAL}
    p.addView(dateRow,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8);bottomMargin=dp(4)})
    val monthColumn=column().apply{gravity=Gravity.CENTER}
    dateRow.addView(monthColumn,LinearLayout.LayoutParams(0,-2,1f))
    val dateLabel=text(if(day==null)"Fora da cobertura" else "${day.day} de ${day.monthName}",20f,cream,true).apply{gravity=Gravity.CENTER}
    monthColumn.addView(dateLabel)
    if(day!=null)monthColumn.addView(text(day.meaning,12f,cream).apply{gravity=Gravity.CENTER})
    dateRow.addView(View(this).apply{setBackgroundColor(0xff956b40.toInt())},LinearLayout.LayoutParams(dp(1),dp(45)))
    val weekdayColumn=column().apply{gravity=Gravity.CENTER}
    dateRow.addView(weekdayColumn,LinearLayout.LayoutParams(0,-2,1f))
    if(day!=null){
     weekdayColumn.addView(text(day.weekday,18f,bronze,true).apply{gravity=Gravity.CENTER})
     weekdayColumn.addView(text(weekdayPortuguese(day.weekday),12f,cream).apply{gravity=Gravity.CENTER})
    }
   }
   val info=horizontal()
   root.addView(info,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(6)})
   val a=column().apply{background=(if(themeIndex==0)artBackground("nurt_year") else null) ?: ForgedFrame(themeIndex);setPadding(dp(9),dp(10),dp(9),dp(10))}
   val b=column().apply{background=(if(themeIndex==0)artBackground("nurt_gregorian") else null) ?: ForgedFrame(themeIndex);setPadding(dp(9),dp(10),dp(9),dp(10))}
   info.addView(a,LinearLayout.LayoutParams(0,dp(70),1f).apply{marginEnd=dp(4)})
   info.addView(b,LinearLayout.LayoutParams(0,dp(70),1f))
   add(a,"ANO ANÃO",11f,bronze,true);add(a,"D.R. ${day?.drYear?:"—"}",17f,cream,true)
   add(b,"DATA GREGORIANA",11f,bronze,true);add(b,greg,15f,cream,true)
  }
  if(tab==1){
   val row=horizontal()
   root.addView(row,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(12)})
   listOf(Triple("◀ Anterior",1f,-1),Triple("Hoje",0.7f,0),Triple("Próximo ▶",1f,1)).forEach{(label,weight,delta)->
    row.addView(action(label){date=if(delta==0)LocalDate.now(ZoneId.systemDefault()) else date.plusDays(delta.toLong())},LinearLayout.LayoutParams(0,-2,weight).apply{marginEnd=dp(3)})
   }
   seasonPanel(root,day)
  }
  if(tab==0){
   seasonPanel(root,day)
   panel(root,12,"nurt_events"){p->
    title(p,"EVENTOS E OBSERVÂNCIAS")
    val content=column().apply{
     gravity=Gravity.START
     setPadding(dp(18),dp(10),dp(18),dp(12))
    }
    val todays=ObservanceEngine.active(this,date)
    if(todays.isNotEmpty()){
     todays.forEach{occurrence->
      val event=occurrence.entry
      content.addView(text(event.getString("name"),17f,cream,true).apply{gravity=Gravity.START})
      content.addView(text(event.optString("pt"),13f,bronze).apply{gravity=Gravity.START})
      content.addView(text(event.optString("descriptionShort"),13f,cream).apply{gravity=Gravity.START})
     }
    }else{
     val upcoming=ObservanceEngine.next(this,date).firstOrNull()
     if(upcoming!=null){
      content.addView(text("PRÓXIMA OBSERVÂNCIA",11f,bronze,true).apply{gravity=Gravity.START})
      content.addView(text(upcoming.entry.getString("name"),17f,cream,true).apply{gravity=Gravity.START})
      content.addView(text(upcoming.start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),12f,bronze).apply{gravity=Gravity.START})
      content.addView(text(upcoming.entry.optString("descriptionShort"),13f,cream).apply{gravity=Gravity.START})
     }else content.addView(text("Nenhuma observância encontrada",13f,cream).apply{gravity=Gravity.START})
    }
    content.addView(text("Ver todos os eventos  ›",12f,bronze,true).apply{gravity=Gravity.START})
    p.addView(content,LinearLayout.LayoutParams(-1,-2))
    content.setOnClickListener{tab=3;draw()}
   }
  }
  if(tab==2)panel(root){title(it,"FASES E MESES LUNARES");add(it,"Dados astronômicos em validação. Nenhuma fase ou porcentagem é apresentada como cálculo confirmado.",14f)}
  if(tab==3){
   val all=ObservanceEngine.catalogue(this,date)
   val fmt=DateTimeFormatter.ofPattern("dd 'de' MMM 'de' yyyy",Locale("pt","BR"))
   fun active(o:ObservanceEngine.Occurrence)=!date.isBefore(o.start)&&!date.isAfter(o.end)
   fun status(o:ObservanceEngine.Occurrence)=if(active(o))"ATIVO" else if(o.start.isAfter(date))"FUTURO" else "PASSADO"
   fun line(parent:LinearLayout,value:String,size:Float,color:Int,bold:Boolean=false){
    if(value.isBlank())return
    parent.addView(text(value,size,color,bold).apply{
     gravity=Gravity.START;textAlignment=View.TEXT_ALIGNMENT_VIEW_START
     setPadding(0,dp(2),0,dp(2))
    },LinearLayout.LayoutParams(-1,-2))
   }
   fun ornament(o:ObservanceEngine.Occurrence):TextView{
    val name=o.entry.optString("pt").lowercase(Locale.ROOT)
    val symbol=when{
     "colheita" in name->"✦"
     "inverno" in name->"❄"
     "queimados" in name->"⚔"
     "forja" in name->"⚒"
     "primavera" in name->"❀"
     else->"◆"
    }
    return text(symbol,29f,bronze,true).apply{
     gravity=Gravity.CENTER
     background=ForgedFrame(themeIndex)
    }
   }
   val selected=all.firstOrNull{it.entry.optString("id")==selectedEventId}
   if(selected!=null){
    panel(root,8){p->
     val back=text("‹  Voltar para a lista",14f,bronze,true)
     back.setPadding(dp(4),dp(12),0,dp(12))
     back.setOnClickListener{selectedEventId=null;draw()}
     p.addView(back)
    }
    panel(root,10,"nurt_events"){card->
     card.gravity=Gravity.START
     card.setPadding(dp(18),dp(18),dp(18),dp(22))
     val hero=ornament(selected)
     card.addView(hero,LinearLayout.LayoutParams(-1,dp(155)).apply{bottomMargin=dp(16)})
     line(card,selected.entry.optString("name"),23f,bronze,true)
     line(card,selected.entry.optString("pt"),17f,cream)
     line(card,status(selected)+"  ·  "+selected.start.format(fmt)+" – "+selected.end.format(fmt),12f,bronze,true)
     lunarDivider(card)
     line(card,"RESUMO",13f,bronze,true)
     line(card,selected.entry.optString("descriptionShort"),15f,cream)
     val full=selected.entry.optString("descriptionLong")
     if(full.isNotBlank()){
      lunarDivider(card)
      line(card,"DESCRIÇÃO COMPLETA",13f,bronze,true)
      line(card,full,15f,cream)
     }
     lunarDivider(card)
     line(card,"TIPO",13f,bronze,true)
     line(card,selected.entry.optString("type","Festival"),14f,cream)
     line(card,"RECORRÊNCIA",13f,bronze,true)
     line(card,selected.entry.optString("recurrence","Anual"),14f,cream)
     val scope=selected.entry.optJSONObject("clanScope")
     line(card,"APLICAÇÃO",13f,bronze,true)
     line(card,if(scope?.optString("mode")=="selected")"Clãs específicos" else "Todos os clãs",14f,cream)
    }
   }else{
    panel(root,8){title(it,"EVENTOS E OBSERVÂNCIAS")}
    if(all.isEmpty())panel(root){line(it,"Nenhum evento encontrado.",14f,cream)}
    all.forEach{o->
     panel(root,6,"nurt_events"){card->
      card.setPadding(dp(10),dp(9),dp(10),dp(9))
      val row=horizontal().apply{gravity=Gravity.CENTER_VERTICAL}
      row.addView(ornament(o),LinearLayout.LayoutParams(dp(66),dp(92)).apply{rightMargin=dp(11)})
      val info=column()
      val top=horizontal().apply{gravity=Gravity.CENTER_VERTICAL}
      val name=text(o.entry.optString("name"),16f,bronze,true).apply{
       maxLines=2
      }
      top.addView(name,LinearLayout.LayoutParams(0,-2,1f))
      val badge=text(status(o),10f,if(active(o))0xffffc879.toInt() else cream,true).apply{
       setPadding(dp(5),dp(4),dp(5),dp(4))
      }
      top.addView(badge)
      info.addView(top)
      line(info,o.entry.optString("pt"),13f,bronze)
      line(info,o.start.format(fmt)+" – "+o.end.format(fmt),11f,cream)
      val summary=text(o.entry.optString("descriptionShort"),12f,cream).apply{
       maxLines=2;ellipsize=android.text.TextUtils.TruncateAt.END
       setPadding(0,dp(4),0,0)
      }
      info.addView(summary)
      row.addView(info,LinearLayout.LayoutParams(0,-2,1f))
      card.addView(row)
      card.setOnClickListener{selectedEventId=o.entry.optString("id");draw()}
     }
    }
   }
  }
  if(tab==4){
   panel(root){title(it,"APARÊNCIA")
    names.forEachIndexed{i,n->it.addView(action((if(themeIndex==i)"◆ " else "◇ ")+n){themeIndex=i;getPreferences(MODE_PRIVATE).edit().putInt("theme",i).apply()})}
   }
   panel(root){title(it,"FUSO HORÁRIO");add(it,ZoneId.systemDefault().id,15f)}
   panel(root){p->
    title(p,"DIA DE DURIN · LOCALIZAÇÃO")
    add(p,savedLocation(),15f)
    p.addView(action("Atualizar localização"){refreshDurinLocation()})
    add(p,"O cálculo astronômico local ainda está em desenvolvimento. Nenhuma data será estimada sem validar o modelo de visibilidade lunar.",12f)
   }
  }
  val tabs=horizontal().apply{background=ForgedFrame(themeIndex);setPadding(dp(4),dp(7),dp(4),dp(7))}
  val labels=arrayOf("Hoje","Calendário","Luas","Eventos","Mais")
  labels.forEachIndexed{i,label->
   val item=column().apply{
    gravity=Gravity.CENTER
    if(tab==i && themeIndex!=0)background=ForgedFrame(themeIndex)
    setOnClickListener{tab=i;draw()}
   }
   val artNames=arrayOf("nav_hoje","nav_calendario","nav_luas","nav_eventos","nav_mais")
   val imageId=resources.getIdentifier(artNames[i],"drawable",packageName)
   if(imageId!=0){
    val symbol=ImageView(this).apply{setImageResource(imageId);scaleType=ImageView.ScaleType.FIT_CENTER}
    item.addView(symbol,LinearLayout.LayoutParams(dp(53),dp(53)))
   }else{
    item.addView(KhazadNavIcon(this,i,tab==i),LinearLayout.LayoutParams(dp(38),dp(38)))
   }
   item.addView(text(label,10f,if(tab==i)bronze else cream,tab==i).apply{setPadding(0,0,0,0)})
   tabs.addView(item,LinearLayout.LayoutParams(0,dp(84),1f))
  }
  outer.addView(tabs,LinearLayout.LayoutParams(-1,-2))
  setContentView(outer)
 }
}
