package com.dwarrow.calendar

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.widget.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.json.JSONArray

private class ForgedFrame(private val mode:Int=0):Drawable(){
 private val p=Paint(3)
 override fun draw(c:Canvas){
  val b=bounds;val w=b.width().toFloat();val h=b.height().toFloat()
  if(w<=0f||h<=0f)return
  val dark=if(mode==2)0xffe7dcca.toInt() else 0xff0b0e12.toInt()
  p.shader=LinearGradient(0f,0f,w,h,intArrayOf(if(mode==2)0xfff9f0df.toInt() else 0xff30353b.toInt(),dark,if(mode==2)0xffd9c7a9.toInt() else 0xff16191d.toInt()),null,Shader.TileMode.CLAMP)
  c.drawRect(0f,0f,w,h,p);p.shader=null
  fun border(inset:Float,color:Int,sw:Float){
   p.color=color;p.style=Paint.Style.STROKE;p.strokeWidth=sw
   c.drawRect(inset,inset,w-inset,h-inset,p);p.style=Paint.Style.FILL
  }
  val gold=if(mode==1)0xffd2a15a.toInt() else 0xffa87743.toInt()
  border(2f,gold,4f);border(7f,0xff40372d.toInt(),2f);border(12f,0xff785637.toInt(),1f)
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

class MainActivity:Activity(){
 private val names=arrayOf("Khazad-dûm","Dornogal","Dwarrow Scholar")
 private val bronze=0xffd3a366.toInt()
 private val cream=0xfff3e6ce.toInt()
 private var themeIndex=0
 private var tab=0
 private var date=LocalDate.now()
 private fun dp(n:Int)=(n*resources.displayMetrics.density+0.5f).toInt()
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);themeIndex=getPreferences(MODE_PRIVATE).getInt("theme",0).coerceIn(0,2);draw()}
 private fun text(value:String,size:Float=16f,color:Int=cream,bold:Boolean=false):TextView=TextView(this).apply{
  text=value;textSize=size;setTextColor(color);gravity=Gravity.CENTER
  if(bold)setTypeface(null,android.graphics.Typeface.BOLD)
  setPadding(dp(7),dp(6),dp(7),dp(6))
 }
 private fun column():LinearLayout=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL}
 private fun panel(root:LinearLayout,space:Int=12,build:(LinearLayout)->Unit){
  val c=column().apply{background=ForgedFrame(themeIndex);setPadding(dp(20),dp(17),dp(20),dp(17))}
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
  val scroll=ScrollView(this).apply{setBackgroundColor(if(themeIndex==2)0xffd4c4aa.toInt() else 0xff0d1014.toInt());isFillViewport=true}
  val root=column().apply{setPadding(dp(12),dp(16),dp(12),dp(10))}
  scroll.addView(root)
  add(root,"◆  "+names[themeIndex].uppercase(Locale.ROOT)+"  ◆",19f,bronze,true)
  add(root,"CALENDÁRIO ANÃO",13f,cream,true)
  add(root,"━━  ◈  ━━",14f,bronze)
  if(tab==0||tab==2){
   panel(root){
    add(it,"☾  CICLO LUNAR  ☽",16f,bronze,true)
    val moon=TextView(this).apply{text="◕";textSize=80f;gravity=Gravity.CENTER;setTextColor(0xffb6b6b5.toInt())}
    it.addView(moon)
    add(it,day?.monthName?:"—",23f,cream,true)
    add(it,day?.meaning?:"Data fora da cobertura",15f,bronze)
    add(it,"Representação lunar decorativa · fase não calculada",11f,0xffaaa59d.toInt())
   }
  }
  if(tab==0||tab==1){
   panel(root){
    add(it,"◆  DATA ANÃ  ◆",16f,bronze,true)
    add(it,if(day==null)"Fora da cobertura" else "${day.day} de ${day.monthName}",31f,cream,true)
    add(it,"━━━━  ◈  ━━━━",12f,bronze)
    if(day!=null){
     add(it,day.weekday,24f,cream,true)
     add(it,day.meaning,15f,bronze)
     add(it,"Dia ${day.day} de ${day.length} · Mês ${day.month}",12f,cream)
    }
   }
   panel(root){
    add(it,"❖  ESTAÇÃO ANÃ",15f,bronze,true)
    add(it,day?.let{d->CalendarEngine.season(this,d)}?:"—",17f)
   }
   panel(root){
    add(it,"ANO ANÃO     ◆     DATA GREGORIANA",12f,bronze,true)
    add(it,"D.R. ${day?.drYear?:"—"}    ·    $greg",16f)
   }
  }
  if(tab==1){
   val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
   root.addView(row,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(12)})
   listOf(Triple("◀ Anterior",1f,-1),Triple("Hoje",0.7f,0),Triple("Próximo ▶",1f,1)).forEach{(label,weight,delta)->
    row.addView(action(label){date=if(delta==0)LocalDate.now(ZoneId.systemDefault()) else date.plusDays(delta.toLong())},LinearLayout.LayoutParams(0,-2,weight).apply{marginEnd=dp(3)})
   }
  }
  if(tab==0){
   panel(root){
    add(it,"✦  EVENTOS E OBSERVÂNCIAS",16f,bronze,true)
    add(it,"◇  Consultar calendário de eventos  ›",16f,cream,true)
    add(it,"Festivais e registros do calendário anão",12f,bronze)
    it.setOnClickListener{tab=3;draw()}
   }
  }
  if(tab==2)panel(root){add(it,"FASES E ILUMINAÇÃO",17f,bronze,true);add(it,"Dados astronômicos em validação. Não exibimos porcentagens sem confirmação.",14f)}
  if(tab==3){
   add(root,"✦  OBSERVÂNCIAS",20f,bronze,true)
   add(root,"Catálogo offline · datas ainda não validadas",12f,cream)
   val entries=JSONArray(assets.open("observances.json").bufferedReader().use{r->r.readText()})
   for(i in 0 until entries.length()){
    val e=entries.getJSONObject(i)
    panel(root){add(it,e.getString("name"),17f,bronze,true);add(it,e.getString("pt"),14f)}
   }
  }
  if(tab==4){
   panel(root){add(it,"◆  APARÊNCIA",18f,bronze,true)
    names.forEachIndexed{i,n->it.addView(action((if(themeIndex==i)"◆ " else "◇ ")+n){themeIndex=i;getPreferences(MODE_PRIVATE).edit().putInt("theme",i).apply()})}
   }
   panel(root){add(it,"FUSO HORÁRIO",14f,bronze,true);add(it,ZoneId.systemDefault().id,15f)}
  }
  val tabs=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
  root.addView(tabs,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5)})
  val labels=arrayOf("⌂\nHoje","▦\nCalendário","☾\nLuas","✧\nEventos","◆\nMais")
  labels.forEachIndexed{i,label->
   val b=text(label,12f,if(tab==i)bronze else cream,tab==i).apply{
    background=ForgedFrame(themeIndex);setPadding(0,dp(9),0,dp(9))
    setOnClickListener{tab=i;draw()}
   }
   tabs.addView(b,LinearLayout.LayoutParams(0,-2,1f).apply{marginEnd=dp(2)})
  }
  setContentView(scroll)
 }
}
