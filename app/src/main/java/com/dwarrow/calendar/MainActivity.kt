package com.dwarrow.calendar

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity:Activity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(36,64,36,24);setBackgroundColor(Color.rgb(32,39,41))}
  fun line(s:String,size:Float,color:Int=Color.WHITE){root.addView(TextView(this).apply{text=s;textSize=size;setTextColor(color);setPadding(0,12,0,12)})}
  line("✦  CALENDÁRIO ANÃO",18f,Color.rgb(211,183,125))
  val today=LocalDate.now(ZoneId.systemDefault());val day=CalendarEngine.lookup(this,today)
  if(day!=null){line("${day.day} de ${day.monthName}",29f);line("D.R. ${day.drYear}",24f,Color.rgb(211,183,125));line(day.weekday,20f);line("${day.meaning} · mês ${day.month} · ${day.length} dias",16f);line(if(day.intercalary)"Ano de 13 meses" else "Ano de 12 meses",16f)}else line("Data fora da cobertura dos dados originais",18f)
  line(today.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy",Locale("pt","BR"))),17f)
  line("Fuso automático: ${ZoneId.systemDefault().id}",14f,Color.LTGRAY)
  line("Prévia offline: mudança de dia à meia-noite. A virada ao nascer do Sol, fases lunares e Dia de Durin ainda não estão validados.",13f,Color.LTGRAY)
  setContentView(root)
 }
}
