package com.dwarrow.calendar

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : Activity() {
 private val themes = arrayOf("Khazad-dûm", "Dornogal", "Dwarrow Scholar")
 private val backs = intArrayOf(0xff141619.toInt(),0xff15232c.toInt(),0xfff2e9d6.toInt())
 private val surfaces = intArrayOf(0xff202328.toInt(),0xff22333e.toInt(),0xfffffbf1.toInt())
 private val golds = intArrayOf(0xffc49a59.toInt(),0xffd7a34e.toInt(),0xff805b32.toInt())
 private val inks = intArrayOf(0xfff5e7ca.toInt(),0xfff5e8d0.toInt(),0xff322c26.toInt())
 private var selected = 0
 private var viewedDate:LocalDate = LocalDate.now()
 private var tab:Int = 0
 private fun dp(value:Int):Int=(value*resources.displayMetrics.density+0.5f).toInt()
 override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState); selected=getPreferences(MODE_PRIVATE).getInt("theme",0).coerceIn(0,2); draw() }
 private fun shape(bg:Int,border:Int,thickness:Int=2,radius:Float=14f):GradientDrawable = GradientDrawable().apply { setColor(bg); cornerRadius=radius;setStroke(thickness,border) }
 private fun draw(){
  val t=selected; val zone=ZoneId.systemDefault();val today=LocalDate.now(zone);val day=CalendarEngine.lookup(this,viewedDate)
  val scroll=ScrollView(this).apply { setBackgroundColor(backs[t]);isFillViewport = true }
  val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(24),dp(18),dp(32)) }
  scroll.addView(root)
  fun text(parent:LinearLayout,value:String,size:Float,color:Int=inks[t],bold:Boolean=false,pad:Int=8):TextView = TextView(this).apply {
    text=value;textSize=size;setTextColor(color);gravity=Gravity.CENTER;textAlignment=View.TEXT_ALIGNMENT_CENTER
    if(bold) setTypeface(null,Typeface.BOLD)
    setPadding(8,pad,8,pad);parent.addView(this)
  }
  text(root,when(t){0->"◆  KHAZAD-DÛM  ◆";1->"◆  DORNOGAL  ◆";else->"✦  DWARROW SCHOLAR  ✦"},16f,golds[t],true,14)
  text(root,"CALENDÁRIO ANÃO",26f,inks[t],true,12)
  text(root,when(t){0->"◆  PEDRA · FORJA · MEMÓRIA  ◆";1->"◆  PEDRA · HONRA · TRADIÇÃO  ◆";else->"✦  REGISTROS DE DURIN  ✦"},12f,golds[t],false,4)
  val main=LinearLayout(this).apply {
   orientation=LinearLayout.VERTICAL
   background=if(t==0) getDrawable(R.drawable.khazad_stone_panel) else shape(surfaces[t],golds[t],3,if(t==2)8f else 18f)
   setPadding(dp(18),dp(24),dp(18),dp(24))
  }
  root.addView(main,LinearLayout.LayoutParams(-1,-2).apply{topMargin=18;bottomMargin=14})
  text(main,"◆  ━━━━━  ◈  ━━━━━  ◆",14f,golds[t])
  text(main,if(day==null)"Data fora da cobertura" else "${day.day} de ${day.monthName}",32f,inks[t],true,14)
  if(day!=null){
   text(main,"D.R. ${day.drYear}",23f,golds[t],true)
   text(main,day.weekday,20f)
   text(main,day.meaning,15f,golds[t])
   text(main,"DIA ${day.day} DE ${day.length}  ·  MÊS ${day.month}",12f,inks[t],true)
  }
  text(main,"◆  ══════  ◈  ══════  ◆",14f,golds[t])
  fun panel(title:String,description:String){
   val card=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;background=if(t==0)getDrawable(R.drawable.khazad_stone_panel) else shape(surfaces[t],golds[t],1,10f);setPadding(dp(15),dp(14),dp(15),dp(14))}
   root.addView(card,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=12})
   text(card,title,17f,golds[t],true,5);text(card,description,15f,inks[t],false,5)
  }
  if(day!=null) panel("❖  ESTAÇÃO ANÃ",CalendarEngine.season(this,day))
  panel("▦  DATA GREGORIANA",viewedDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy",Locale("pt","BR"))))
  val navigation=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER }
  root.addView(navigation,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=12})
  fun nav(label:String,weight:Float,action:()->Unit){
   val b=TextView(this).apply {
    text=label;textSize=15f;setTextColor(inks[t]);gravity=Gravity.CENTER
    setPadding(4,18,4,18);background=shape(surfaces[t],golds[t],1,8f)
    setOnClickListener{action();draw()}
   }
   navigation.addView(b,LinearLayout.LayoutParams(0,-2,weight).apply{marginEnd=6})
  }
  nav("◀  Anterior",1f){viewedDate=viewedDate.minusDays(1)}
  nav("Hoje",0.65f){viewedDate=LocalDate.now(ZoneId.systemDefault())}
  nav("Próximo  ▶",1f){viewedDate=viewedDate.plusDays(1)}
  text(root,"CONSULTA DE DATAS · ${viewedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",13f,golds[t],false,5)
  if(tab==4) panel("◷  FUSO HORÁRIO",zone.id)
  if(tab==4) text(root,"ESCOLHA A APARÊNCIA",15f,golds[t],true,18)
  val choices=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  if(tab==4) root.addView(choices)
  themes.forEachIndexed{index,name->
   val button=TextView(this).apply {
    text=(if(index==t)"◆  " else "◇  ")+name;textSize=17f;gravity=Gravity.CENTER
    setTextColor(inks[t]);setPadding(12,17,12,17)
    background=shape(if(index==t)golds[t] and 0x22ffffff else surfaces[t],golds[t],if(index==t)2 else 1,9f)
    setOnClickListener{selected=index;getPreferences(MODE_PRIVATE).edit().putInt("theme",index).apply();draw()}
   }
   choices.addView(button,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=10})
  }
  if(tab==3) {
   text(root,"CATÁLOGO DE OBSERVÂNCIAS · DATAS A VALIDAR",13f,golds[t],true)
   val events=org.json.JSONArray(assets.open("observances.json").bufferedReader().use{it.readText()})
   for(i in 0 until events.length()){
    val e=events.getJSONObject(i)
    panel("✦  ${e.getString("name")}",e.getString("pt"))
   }
  }
  if(tab==2) panel("☾  CICLOS LUNARES","A data do mês lunar vem do calendário offline. A fase e a iluminação ainda exigem validação antes de serem exibidas.")
  if(tab==4) text(root,"Widgets independentes: escolha Khazad-dûm, Dornogal ou Dwarrow Scholar na lista de widgets do Android.",13f,inks[t],false,14)
  val tabs=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER}
  root.addView(tabs,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(16)})
  val tabNames=arrayOf("⌂ Hoje","▦ Datas","☾ Luas","✦ Eventos","⋯ Mais")
  tabNames.forEachIndexed{index,label->
   val b=TextView(this).apply{
    text=label;textSize=11f;gravity=Gravity.CENTER;setTextColor(if(tab==index)golds[t] else inks[t])
    setPadding(dp(1),dp(13),dp(1),dp(13))
    background=shape(surfaces[t],if(tab==index)golds[t] else surfaces[t],if(tab==index)2 else 1,4f)
    setOnClickListener{tab=index;draw()}
   }
   tabs.addView(b,LinearLayout.LayoutParams(0,-2,1f).apply{marginEnd=dp(2)})
  }
  setContentView(scroll)
 }
}
