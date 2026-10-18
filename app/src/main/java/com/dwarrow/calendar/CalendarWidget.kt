package com.dwarrow.calendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.ZoneId

abstract class BaseCalendarWidget(private val style:Int):AppWidgetProvider(){
 override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray){super.onUpdate(context,manager,ids);update(context,manager,ids)}
 override fun onEnabled(context:Context){super.onEnabled(context);val manager=AppWidgetManager.getInstance(context);update(context,manager,manager.getAppWidgetIds(ComponentName(context,javaClass)))}
 override fun onReceive(context:Context,intent:Intent){super.onReceive(context,intent)
  if(intent.action in arrayOf(Intent.ACTION_DATE_CHANGED,Intent.ACTION_TIMEZONE_CHANGED,Intent.ACTION_TIME_CHANGED)){
   val mgr=AppWidgetManager.getInstance(context)
   update(context,mgr,mgr.getAppWidgetIds(ComponentName(context,javaClass)))
  }
 }
 private fun update(context:Context,manager:AppWidgetManager,ids:IntArray){
  val today=LocalDate.now(ZoneId.systemDefault())
  val day=CalendarEngine.lookup(context,today)
  val season=day?.let{CalendarEngine.season(context,it)}?.split(" · ",limit=2) ?: emptyList()
  val seasonName=season.getOrNull(0) ?: ""
  val icon=when(seasonName){
   "Iklaladrân"->R.drawable.estacao_iklaladran
   "Shulukadrân"->R.drawable.estacao_shulukadran
   "Kêlithadrân"->R.drawable.estacao_kelithadran
   "’Arsadrân"->R.drawable.estacao_arsadran
   "Dabladrân"->R.drawable.estacao_dabladran
   else->R.drawable.estacao_injamadran
  }
  val names=arrayOf("Lua nova","Lua crescente","Quarto crescente","Gibosa crescente","Lua cheia","Gibosa minguante","Quarto minguante","Lua minguante")
  val images=intArrayOf(R.drawable.lua_nova,R.drawable.lua_crescente,R.drawable.quarto_crescente,R.drawable.gibosa_crescente,R.drawable.lua_cheia,R.drawable.gibosa_minguante,R.drawable.quarto_minguante,R.drawable.lua_minguante)
  val elapsed=java.time.temporal.ChronoUnit.SECONDS.between(java.time.Instant.parse("2000-01-06T18:14:00Z"),today.atTime(12,0).atZone(ZoneId.systemDefault()).toInstant())/86400.0
  val age=((elapsed%29.530588853)+29.530588853)%29.530588853
  val phase=kotlin.math.floor(age/29.530588853*8+0.5).toInt()%8
  val weekday=when(day?.weekday){
   "Izgilnurt"->"segunda-feira";"Akhlathnurt"->"terça-feira";"Adadnurt"->"quarta-feira"
   "Thanbnurt"->"quinta-feira";"Abkân-nurt"->"sexta-feira";"Thatrnurt"->"sábado"
   "Ibriznurt"->"domingo";else->""
  }
  for(id in ids){
   val v=RemoteViews(context.packageName,R.layout.widget)
   val approvedFrame=context.resources.getIdentifier("widget_nurtfahani_moldura_aprovada","drawable",context.packageName)
   if(approvedFrame!=0)v.setInt(R.id.widgetRoot,"setBackgroundResource",approvedFrame)
   v.setImageViewResource(R.id.widgetMoon,images[phase])
   v.setTextViewText(R.id.widgetMoonLabel,names[phase])
   v.setTextViewText(R.id.widgetMoonKh,"")
   v.setTextViewText(R.id.widgetDate,if(day==null)"Fora do período" else "${day.day} de ${day.monthName}")
   v.setTextViewText(R.id.widgetMonthMeaning,day?.meaning ?: "")
   v.setTextViewText(R.id.widgetWeekKh,day?.weekday ?: "")
   v.setTextViewText(R.id.widgetWeekPt,weekday)
   v.setTextViewText(R.id.widgetGregorian,today.format(java.time.format.DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy",java.util.Locale("pt","BR"))))
   v.setImageViewResource(R.id.widgetSeasonIcon,icon)
   v.setTextViewText(R.id.widgetSeasonPt,season.getOrNull(1) ?: "")
   v.setTextViewText(R.id.widgetSeasonKh,seasonName)
   val open=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
   v.setOnClickPendingIntent(R.id.widgetRoot,open)
   manager.updateAppWidget(id,v)
  }
 }
}
class KhazadWidget:BaseCalendarWidget(0)
class DornogalWidget:BaseCalendarWidget(1)
class ScholarWidget:BaseCalendarWidget(2)
