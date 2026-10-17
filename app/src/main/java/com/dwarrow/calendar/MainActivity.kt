package com.dwarrow.calendar

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
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
 override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState); selected=getPreferences(MODE_PRIVATE).getInt("theme",0).coerceIn(0,2); draw() }
 private fun shape(bg:Int,border:Int,thickness:Int=2,radius:Float=14f):GradientDrawable = GradientDrawable().apply { setColor(bg); cornerRadius=radius;setStroke(thickness,border) }
 private fun draw(){
  val t=selected; val zone=ZoneId.systemDefault();val today=LocalDate.now(zone);val day=CalendarEngine.lookup(this,today)
  val scroll=ScrollView(this).apply { setBackgroundColor(backs[t]);isFillViewport = true }
  val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(22,32,22,40) }
  scroll.addView(root)
  fun text(parent:LinearLayout,value:String,size:Float,color:Int=inks[t],bold:Boolean=false,pad:Int=8):TextView = TextView(this).apply {
    text=value;textSize=size;setTextColor(color);gravity=Gravity.CENTER;textAlignment=View.TEXT_ALIGNMENT_CENTER
    if(bold) setTypeface(null,Typeface.BOLD)
    setPadding(8,pad,8,pad);parent.addView(this)
  }
  text(root,when(t){0->"◆  KHAZAD-DÛM  ◆";1->"◆  DORNOGAL  ◆";else->"✦  DWARROW SCHOLAR  ✦"},16f,golds[t],true,14)
  text(root,"CALENDÁRIO ANÃO",22f,inks[t],true,12)
  val main=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;background=shape(surfaces[t],golds[t],3,if(t==2)8f else 18f);setPadding(10,22,10,22)}
  root.addView(main,LinearLayout.LayoutParams(-1,-2).apply{topMargin=18;bottomMargin=14})
  text(main,"✦  ━━━━━━━━━  ✦",14f,golds[t])
  text(main,if(day==null)"Data fora da cobertura" else "${day.day} de ${day.monthName}",32f,inks[t],true,14)
  if(day!=null){text(main,"D.R. ${day.drYear}",24f,golds[t],true);text(main,day.weekday,19f);text(main,"${day.meaning}  ·  Mês ${day.month}  ·  ${day.length} dias",14f)}
  text(main,"✦  ━━━━━━━━━  ✦",14f,golds[t])
  fun panel(title:String,description:String){
   val card=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;background=shape(surfaces[t],golds[t],1,10f);setPadding(12,10,12,10)}
   root.addView(card,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=12})
   text(card,title,17f,golds[t],true,5);text(card,description,15f,inks[t],false,5)
  }
  panel("◈  CONTAGEM ANÃ",if(day==null)"—" else if(day.intercalary)"Ano de 13 meses" else "Ano de 12 meses")
  panel("◈  DATA GREGORIANA",today.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy",Locale("pt","BR"))))
  panel("◈  FUSO HORÁRIO",zone.id)
  text(root,"ESCOLHA A APARÊNCIA",15f,golds[t],true,18)
  val choices=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  root.addView(choices)
  themes.forEachIndexed{index,name->
   val button=TextView(this).apply {
    text=(if(index==t)"◆  " else "◇  ")+name;textSize=17f;gravity=Gravity.CENTER
    setTextColor(inks[t]);setPadding(12,17,12,17)
    background=shape(if(index==t)golds[t] and 0x22ffffff else surfaces[t],golds[t],if(index==t)3 else 1,9f)
    setOnClickListener{selected=index;getPreferences(MODE_PRIVATE).edit().putInt("theme",index).apply();draw()}
   }
   choices.addView(button,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=10})
  }
  text(root,"Widgets independentes: escolha Khazad-dûm, Dornogal ou Dwarrow Scholar na lista de widgets do Android.",13f,inks[t],false,14)
  setContentView(scroll)
 }
}
