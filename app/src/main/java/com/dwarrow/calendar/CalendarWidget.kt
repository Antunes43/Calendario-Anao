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
 override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray){update(context,manager,ids)}
 override fun onReceive(context:Context,intent:Intent){super.onReceive(context,intent)
  if(intent.action in arrayOf(Intent.ACTION_DATE_CHANGED,Intent.ACTION_TIMEZONE_CHANGED,Intent.ACTION_TIME_CHANGED)){
   val mgr=AppWidgetManager.getInstance(context)
   update(context,mgr,mgr.getAppWidgetIds(ComponentName(context,javaClass)))
  }
 }
 private fun update(context:Context,manager:AppWidgetManager,ids:IntArray){
  val d=CalendarEngine.lookup(context,LocalDate.now(ZoneId.systemDefault()))
  val bg=intArrayOf(R.drawable.widget_khazad,R.drawable.widget_dornogal,R.drawable.widget_scholar)[style]
  val accent=intArrayOf(0xffc49a59.toInt(),0xffd7a34e.toInt(),0xff805b32.toInt())[style]
  val ink=if(style==2)0xff322c26.toInt() else 0xfff5e7ca.toInt()
  for(id in ids){val v=RemoteViews(context.packageName,R.layout.widget)
   v.setInt(R.id.widgetRoot,"setBackgroundResource",bg)
   v.setTextViewText(R.id.widgetTitle,arrayOf("◆ KHAZAD-DÛM ◆","◆ DORNOGAL ◆","✦ DWARROW SCHOLAR ✦")[style])
   v.setTextColor(R.id.widgetTitle,accent);v.setTextColor(R.id.widgetDate,ink);v.setTextColor(R.id.widgetSub,accent)
   v.setTextViewText(R.id.widgetDate,if(d==null)"Fora do período" else "${d.day} de ${d.monthName}")
   v.setTextViewText(R.id.widgetSub,if(d==null)"" else "D.R. ${d.drYear}  ·  ${d.weekday}")
   val open=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
   v.setOnClickPendingIntent(R.id.widgetRoot,open)
   manager.updateAppWidget(id,v)
  }
 }
}
class KhazadWidget:BaseCalendarWidget(0)
class DornogalWidget:BaseCalendarWidget(1)
class ScholarWidget:BaseCalendarWidget(2)
