package com.dwarrow.calendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.ZoneId

class CalendarWidget:AppWidgetProvider(){
 override fun onReceive(context:Context,intent:Intent){super.onReceive(context,intent)
  if(intent.action==Intent.ACTION_DATE_CHANGED || intent.action==Intent.ACTION_TIMEZONE_CHANGED || intent.action==Intent.ACTION_TIME_CHANGED){
   val manager=AppWidgetManager.getInstance(context)
   val ids=manager.getAppWidgetIds(android.content.ComponentName(context,CalendarWidget::class.java))
   update(context,manager,ids)
  }
 }
 override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray){update(context,manager,ids)}
 private fun update(context:Context,manager:AppWidgetManager,ids:IntArray){
  val d=CalendarEngine.lookup(context,LocalDate.now(ZoneId.systemDefault()))
  for(id in ids){val v=RemoteViews(context.packageName,R.layout.widget)
   v.setTextViewText(R.id.widgetDate,if(d==null)"Fora do período" else "${d.day} de ${d.monthName}")
   v.setTextViewText(R.id.widgetSub,if(d==null)"" else "D.R. ${d.drYear}  •  ${d.weekday}")
   val open=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
   v.setOnClickPendingIntent(R.id.widgetTitle,open);v.setOnClickPendingIntent(R.id.widgetDate,open)
   manager.updateAppWidget(id,v)
  }
 }
}
