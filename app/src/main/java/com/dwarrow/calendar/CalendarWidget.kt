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
 // Draw a responsive frame. No bitmap corners or source interior are stretched.
 // The source is retained only as four ornamental corner cutouts.
 private fun widgetFrame(context:Context,width:Int,height:Int):android.graphics.Bitmap{
  val output=android.graphics.Bitmap.createBitmap(width,height,android.graphics.Bitmap.Config.ARGB_8888)
  val canvas=android.graphics.Canvas(output)
  val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
  val accent=when(style){1->0xff9b7759.toInt();2->0xffb8a071.toInt();else->0xffbb8447.toInt()}
  canvas.drawColor(0xff0b0d10.toInt())
  paint.color=0xff11151a.toInt()
  canvas.drawRoundRect(3f,3f,width-3f,height-3f,12f,12f,paint)
  paint.style=android.graphics.Paint.Style.STROKE
  paint.strokeWidth=2f
  paint.color=accent
  canvas.drawRoundRect(5f,5f,width-5f,height-5f,11f,11f,paint)
  paint.strokeWidth=1f
  paint.color=0xff4a382b.toInt()
  canvas.drawRoundRect(10f,10f,width-10f,height-10f,9f,9f,paint)
  paint.style=android.graphics.Paint.Style.FILL
  // A small fixed-size corner ornament is never rescaled to the widget height.
  val source=android.graphics.BitmapFactory.decodeResource(context.resources,R.drawable.widget_nurtfahani_moldura_aprovada)
  val srcSize=(source.width*0.105f).toInt().coerceAtMost(source.height/4)
  val corner=(kotlin.math.min(width,height)*0.11f).toInt().coerceIn(15,35)
  val srcRects=arrayOf(
   android.graphics.Rect(0,0,srcSize,srcSize),
   android.graphics.Rect(source.width-srcSize,0,source.width,srcSize),
   android.graphics.Rect(0,source.height-srcSize,srcSize,source.height),
   android.graphics.Rect(source.width-srcSize,source.height-srcSize,source.width,source.height)
  )
  val dstRects=arrayOf(
   android.graphics.Rect(0,0,corner,corner),
   android.graphics.Rect(width-corner,0,width,corner),
   android.graphics.Rect(0,height-corner,corner,height),
   android.graphics.Rect(width-corner,height-corner,width,height)
  )
  for(i in 0..3)canvas.drawBitmap(source,srcRects[i],dstRects[i],paint)
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
