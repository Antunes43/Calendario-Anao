package com.dwarrow.calendar

import android.content.Context
import org.json.JSONObject
import org.json.JSONArray
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Offline lookup based exclusively on original core.json month/year intervals. */
object CalendarEngine {
 data class Day(val drYear:Int,val month:Int,val monthName:String,val meaning:String,val day:Int,val weekday:String,val intercalary:Boolean,val length:Int)

 data class MonthInfo(val number:Int,val name:String,val meaning:String,val start:LocalDate,val end:LocalDate,val length:Int)
 fun monthsForYear(ctx:Context,year:Int):List<MonthInfo>{
  val source=core(ctx).getJSONArray("months")
  val found=ArrayList<MonthInfo>()
  for(i in 0 until source.length()){
   val m=source.getJSONObject(i)
   if(m.getInt("drYear")!=year)continue
   found.add(MonthInfo(m.getInt("monthNumber"),m.getString("name"),m.optString("meaning"),LocalDate.parse(m.getString("startDate")),LocalDate.parse(m.getString("endDate")),m.getInt("lengthDays")))
  }
  return found.sortedBy{it.number}
 }
 private var root:JSONObject?=null
 private var seasons:JSONArray?=null
 private fun core(ctx:Context):JSONObject {
  if(root==null) root=JSONObject(ctx.assets.open("core.json").bufferedReader().use{it.readText()})
  return root!!
 }
 fun lookup(ctx:Context,date:LocalDate=LocalDate.now(ZoneId.systemDefault())):Day? {
  val c=core(ctx); val months=c.getJSONArray("months"); val iso=date.toString()
  var lo=0; var hi=months.length()-1
  while(lo<=hi){
   val mid=(lo+hi) ushr 1; val m=months.getJSONObject(mid)
   when { iso<m.getString("startDate")->hi=mid-1; iso>m.getString("endDate")->lo=mid+1
    else -> {
     val y=m.getInt("drYear"); val years=c.getJSONArray("years")
     var intercalary=false
     for(i in 0 until years.length()){val yr=years.getJSONObject(i);if(yr.getInt("drYear")==y){intercalary=yr.getBoolean("intercalary");break}}
     val offset=ChronoUnit.DAYS.between(LocalDate.parse(m.getString("startDate")),date).toInt()
     val weekdayName=when(date.dayOfWeek.value){1->"Izgilnurt";2->"Akhlathnurt";3->"Adadnurt";4->"Thanbnurt";5->"Abkân-nurt";6->"Thatrnurt";else->"Ibriznurt"}
     return Day(y,m.getInt("monthNumber"),m.getString("name"),m.getString("meaning"),offset+1,weekdayName,intercalary,m.getInt("lengthDays"))
    }
   }
  }
  return null
 }
 fun season(ctx:Context,d:Day):String {
  if(seasons==null) seasons=JSONArray(ctx.assets.open("seasons.json").bufferedReader().use{it.readText()})
  val entries=seasons!!
  fun ordinal(month:Int,day:Int):Int = month*100+day
  for(i in 0 until entries.length()){
   val entry=entries.getJSONObject(i)
   val start=entry.getJSONArray("start")
   val end=entry.getJSONArray("end")
   val startMonth=start.getInt(0)
   val endMonth=end.getInt(0)
   val startDay=start.getInt(1)
   val endDay=if(end.getInt(1)==-1) {
    if(d.month==endMonth) d.length else 99
   } else end.getInt(1)
   val current=ordinal(d.month,d.day)
   val first=ordinal(startMonth,startDay)
   val last=ordinal(endMonth,endDay)
   val matches=if(first<=last) current in first..last else current>=first || current<=last
   if(matches) return "${entry.getString("name")} · ${entry.getString("pt")}"
  }
  return "Estação não identificada"
 }
}
