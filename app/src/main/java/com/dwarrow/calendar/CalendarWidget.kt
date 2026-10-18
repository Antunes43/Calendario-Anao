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
 // Nine-slice: preserve ornamental corners at their original aspect ratio.
 // Only the central stretches are resampled; never crop the source frame.
 private fun widgetFrame(context:Context,width:Int,height:Int):android.graphics.Bitmap{
  val source=android.graphics.BitmapFactory.decodeResource(context.resources,R.drawable.widget_nurtfahani_moldura_aprovada)
  val output=android.graphics.Bitmap.createBitmap(width,height,android.graphics.Bitmap.Config.ARGB_8888)
  val canvas=android.graphics.Canvas(output)
  val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
  val sw=source.width;val sh=source.height
  // Keep corners proportional to the final height. Avoid stretching square gems.
  val sourceCornerX=(sw*0.095f).toInt()
  val sourceCornerY=(sh*0.19f).toInt()
  val cornerScale=kotlin.math.min(1f,kotlin.math.min(
   width.toFloat()/(sourceCornerX*2+1),height.toFloat()/(sourceCornerY*2+1)))
  val destCornerX=(sourceCornerX*cornerScale).toInt().coerceAtLeast(1)
  val destCornerY=(sourceCornerY*cornerScale).toInt().coerceAtLeast(1)
  val xs=intArrayOf(0,sourceCornerX,sw-sourceCornerX,sw)
  val ys=intArrayOf(0,sourceCornerY,sh-sourceCornerY,sh)
  val xd=intArrayOf(0,destCornerX,width-destCornerX,width)
  val yd=intArrayOf(0,destCornerY,height-destCornerY,height)
  for(y in 0..2)for(x in 0..2){
   canvas.drawBitmap(source,android.graphics.Rect(xs[x],ys[y],xs[x+1],ys[y+1]),
    android.graphics.Rect(xd[x],yd[y],xd[x+1],yd[y+1]),paint)
  }
  source.recycle()
  return output
 }
 override fun onAppWidgetOptionsChanged(context:Context,manager:AppWidgetManager,appWidgetId:Int,newOptions:android.os.Bundle){
  super.onAppWidgetOptionsChanged(context,manager,appWidgetId,newOptions)
  update(context,manager,intArrayOf(appWidgetId))
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
   val options=manager.getAppWidgetOptions(id)
   val w=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,320).coerceIn(180,600)
   val h=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,190).coerceIn(100,440)
   v.setImageViewBitmap(R.id.widgetFrame,widgetFrame(context,480,(480f*h/w).toInt().coerceIn(150,900)))
   v.setImageViewResource(R.id.widgetMoon,images[phase])
   val phaseViews=intArrayOf(R.id.widgetPhase0,R.id.widgetPhase1,R.id.widgetPhase2,R.id.widgetPhase3,R.id.widgetPhase4,R.id.widgetPhase5,R.id.widgetPhase6,R.id.widgetPhase7)
   for(i in phaseViews.indices)v.setInt(phaseViews[i],"setImageAlpha",if(i==phase)255 else 85)
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
