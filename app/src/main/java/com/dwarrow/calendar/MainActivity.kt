package com.dwarrow.calendar

import android.app.Activity
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
private class ProportionalArt(private val bitmap:Bitmap):Drawable(){
 private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 override fun draw(canvas:Canvas){
  val b=bounds
  if(b.isEmpty)return
  canvas.drawColor(0xff090b0f.toInt())
  val scale=kotlin.math.min(b.width().toFloat()/bitmap.width,b.height().toFloat()/bitmap.height)
  val width=bitmap.width*scale
  val height=bitmap.height*scale
  val left=b.left+(b.width()-width)/2f
  val top=b.top+(b.height()-height)/2f
  canvas.drawBitmap(bitmap,null,RectF(left,top,left+width,top+height),paint)
 }
 override fun setAlpha(alpha:Int){paint.alpha=alpha}
 override fun setColorFilter(filter:android.graphics.ColorFilter?){paint.colorFilter=filter}
 @Deprecated("Deprecated in Java") override fun getOpacity():Int=PixelFormat.OPAQUE
}
class MainActivity:Activity(){
 private val names=arrayOf("Khazad-dûm","Dornogal","Dwarrow Scholar")
 private val bronze=0xffd8a35f.toInt()
 private val cream=0xfff3e8d7.toInt()
 private var themeIndex=0
 private var tab=0
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
  setPadding(dp(7),dp(6),dp(7),dp(6))
 }
 private fun column():LinearLayout=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL}
 private fun horizontal():LinearLayout=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
 private fun title(root:LinearLayout,heading:String){add(root,"✦  $heading  ✦",13f,bronze,true)}
 private fun artBackground(name:String):Drawable? { val id=resources.getIdentifier(name,"drawable",packageName);if(id==0)return null;val bitmap=android.graphics.BitmapFactory.decodeResource(resources,id) ?: return null;return ProportionalArt(bitmap) }
 private fun panel(root:LinearLayout,space:Int=12,art:String?=null,build:(LinearLayout)->Unit){
  val c=column().apply{background=(if(themeIndex==0) art?.let{artBackground(it)} else null) ?: ForgedFrame(themeIndex);setPadding(dp(13),dp(14),dp(13),dp(14))}
  root.addView(c,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(space)})
  build(c)
 }
 private fun add(parent:LinearLayout,value:String,size:Float=16f,color:Int=cream,bold:Boolean=false){parent.addView(text(value,size,color,bold))}
 private fun action(label:String,onClick:()->Unit):TextView=text(label,13f,bronze,true).apply{
  background=ForgedFrame(themeIndex);setPadding(dp(8),dp(15),dp(8),dp(15));setOnClickListener{onClick();draw()}
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
  panel(root,10,"nurt_header"){h->
   h.minimumHeight=dp(132)
   h.gravity=Gravity.CENTER
   add(h,if(themeIndex==0)"NURTFAHANI" else names[themeIndex].uppercase(Locale.ROOT),24f,bronze,true)
   add(h,"C A L E N D Á R I O   A N Ã O",11f,cream,true)
  }
  if(tab==0||tab==2){
   panel(root,10,"nurt_lunar"){p->
    val phase=lunarPhase(date)
    val index=phase.first
    val line=horizontal()
    p.addView(line,LinearLayout.LayoutParams(-1,dp(132)))
    val left=column().apply{gravity=Gravity.CENTER}
    line.addView(left,LinearLayout.LayoutParams(0,-2,0.30f))
    add(left,"FASE ATUAL",10f,bronze,true)
    add(left,lunarNames[index],11f,cream)
    val imageId=resources.getIdentifier(lunarImages[index],"drawable",packageName)
    val moon=ImageView(this).apply{
     if(imageId!=0)setImageResource(imageId) else setImageDrawable(KhazadMoon())
     scaleType=ImageView.ScaleType.FIT_CENTER
     contentDescription=lunarNames[index]
    }
    line.addView(moon,LinearLayout.LayoutParams(0,dp(125),0.40f))
    val right=column().apply{gravity=Gravity.CENTER}
    line.addView(right,LinearLayout.LayoutParams(0,-2,0.29f))
    add(right,"PRÓXIMA FASE",10f,bronze,true)
    add(right,lunarNames[(index+1)%8],10f,cream)
    add(right,"≈ ${phase.second} dia(s)",10f,bronze)
   }
  }
  if(tab==0||tab==1){
   panel(root,9,"nurt_date"){p->
    title(p,"NURT KHUZDUL")
    add(p,if(day==null)"Fora da cobertura" else "${day.day} de ${day.monthName}",26f,cream,true)
    if(day!=null)add(p,day.meaning,14f,cream)
    add(p,"─── ◈ ───",12f,bronze)
    if(day!=null){
     add(p,day.weekday,20f,bronze,true)
     add(p,weekdayPortuguese(day.weekday),14f,cream)
    }
   }
   val info=horizontal()
   root.addView(info,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(10)})
   val a=column().apply{background=(if(themeIndex==0)artBackground("nurt_year") else null) ?: ForgedFrame(themeIndex);setPadding(dp(5),dp(11),dp(5),dp(11))}
   val b=column().apply{background=(if(themeIndex==0)artBackground("nurt_gregorian") else null) ?: ForgedFrame(themeIndex);setPadding(dp(8),dp(12),dp(8),dp(12))}
   info.addView(a,LinearLayout.LayoutParams(0,-2,1f).apply{marginEnd=dp(4)})
   info.addView(b,LinearLayout.LayoutParams(0,-2,1f))
   add(a,"ANO ANÃO",11f,bronze,true);add(a,"D.R. ${day?.drYear?:"—"}",17f,cream,true)
   add(b,"DATA GREGORIANA",10f,bronze,true);add(b,greg,13f,cream)
  }
  if(tab==1){
   val row=horizontal()
   root.addView(row,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(12)})
   listOf(Triple("◀ Anterior",1f,-1),Triple("Hoje",0.7f,0),Triple("Próximo ▶",1f,1)).forEach{(label,weight,delta)->
    row.addView(action(label){date=if(delta==0)LocalDate.now(ZoneId.systemDefault()) else date.plusDays(delta.toLong())},LinearLayout.LayoutParams(0,-2,weight).apply{marginEnd=dp(3)})
   }
   panel(root){title(it,"ESTAÇÃO ANÃ");add(it,day?.let{d->CalendarEngine.season(this,d)}?:"—",17f)}
  }
  if(tab==0){
   panel(root,12,"nurt_events"){p->
    title(p,"EVENTOS E OBSERVÂNCIAS")
    val link=horizontal().apply{setPadding(dp(10),dp(10),dp(10),dp(10))}
    link.addView(text("✧",29f,bronze),LinearLayout.LayoutParams(dp(50),-2))
    val t=column();link.addView(t,LinearLayout.LayoutParams(0,-2,1f))
    add(t,"Consultar calendário oficial  ›",13f,cream,true)
    add(t,"Festivais e observâncias registradas",10f,0xffc5b4a0.toInt())
    p.addView(link,LinearLayout.LayoutParams(-1,-2))
    link.setOnClickListener{tab=3;draw()}
   }
  }
  if(tab==2)panel(root){title(it,"FASES E MESES LUNARES");add(it,"Dados astronômicos em validação. Nenhuma fase ou porcentagem é apresentada como cálculo confirmado.",14f)}
  if(tab==3){
   panel(root){title(it,"EVENTOS E OBSERVÂNCIAS");add(it,"Catálogo offline · datas ainda não validadas",12f,cream)}
   val entries=JSONArray(assets.open("observances.json").bufferedReader().use{r->r.readText()})
   for(i in 0 until entries.length()){
    val e=entries.getJSONObject(i)
    panel(root,8){add(it,"✧  "+e.getString("name"),17f,bronze,true);add(it,e.getString("pt"),14f)}
   }
  }
  if(tab==4){
   panel(root){title(it,"APARÊNCIA")
    names.forEachIndexed{i,n->it.addView(action((if(themeIndex==i)"◆ " else "◇ ")+n){themeIndex=i;getPreferences(MODE_PRIVATE).edit().putInt("theme",i).apply()})}
   }
   panel(root){title(it,"FUSO HORÁRIO");add(it,ZoneId.systemDefault().id,15f)}
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
