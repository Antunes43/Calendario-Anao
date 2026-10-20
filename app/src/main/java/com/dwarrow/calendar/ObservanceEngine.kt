package com.dwarrow.calendar

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Resolves observance intervals from the same dated months used by CalendarEngine. */
object ObservanceEngine {
 data class Occurrence(val entry:JSONObject,val start:LocalDate,val end:LocalDate)
 private data class Month(val start:LocalDate,val length:Int)
 private class Data(ctx:Context) {
  val core=JSONObject(ctx.assets.open("core.json").bufferedReader().use{it.readText()})
  val entries=JSONArray(ctx.assets.open("observances.json").bufferedReader().use{it.readText()})
  val years=HashMap<Int,Int>()
  val months=HashMap<Pair<Int,Int>,Month>()
  init {
   val ys=core.getJSONArray("years")
   for(i in 0 until ys.length()){
    val y=ys.getJSONObject(i)
    years[y.getInt("drYear")]=y.getInt("monthCount")
   }
   val ms=core.getJSONArray("months")
   for(i in 0 until ms.length()){
    val m=ms.getJSONObject(i)
    months[m.getInt("drYear") to m.getInt("monthNumber")]=
     Month(LocalDate.parse(m.getString("startDate")),m.getInt("lengthDays"))
   }
  }
 }
 private var cache:Data?=null
 private fun data(ctx:Context):Data=cache ?: Data(ctx.applicationContext).also{cache=it}
 private fun date(d:Data,year:Int,rule:JSONObject):LocalDate? {
  val month=d.months[year to rule.getInt("month")]?:return null
  val day=when(rule.getString("dayRule")){
   "last_day"->month.length
   "specific_day"->rule.optInt("day",0)
   else->return null
  }
  if(day !in 1..month.length)return null
  return month.start.plusDays((day-1).toLong())
 }
 private fun recurs(entry:JSONObject,year:Int):Boolean {
  val r=entry.optJSONObject("recurrence")?:return true
  return when(r.optString("type","annual")){
   "annual"->true
   "one_time"->year==r.optInt("oneTimeDrYear",Int.MIN_VALUE)
   "interval"->{
    val every=r.optInt("intervalYears",0)
    val anchor=r.optInt("intervalAnchorDrYear",Int.MIN_VALUE)
    every>0 && anchor!=Int.MIN_VALUE && year>=anchor && (year-anchor)%every==0
   }
   "specific_years"->{
    val years=r.optJSONArray("specificDrYears")?:return false
    (0 until years.length()).any{years.optInt(it,Int.MIN_VALUE)==year}
   }
   else->false
  }
 }
 private fun occurrence(d:Data,e:JSONObject,year:Int):Occurrence? {
  if(!e.optBoolean("active",true)||!recurs(e,year))return null
  val count=d.years[year]?:return null
  val rules=e.optJSONObject("yearRules")?.optJSONObject(if(count==13)"month13" else "month12")?:return null
  val start=date(d,year,rules.getJSONObject("start"))?:return null
  val end=date(d,year+rules.optInt("endYearOffset",0),rules.getJSONObject("end"))?:return null
  if(end.isBefore(start))return null
  return Occurrence(e,start,end)
 }
 fun active(ctx:Context,on:LocalDate):List<Occurrence> {
  val day=CalendarEngine.lookup(ctx,on)?:return emptyList()
  val d=data(ctx)
  val result=ArrayList<Occurrence>()
  for(i in 0 until d.entries.length()){
   val e=d.entries.getJSONObject(i)
   for(y in (day.drYear-1)..day.drYear){
    val o=occurrence(d,e,y)?:continue
    if(!on.isBefore(o.start)&&!on.isAfter(o.end))result.add(o)
   }
  }
  return result.sortedBy{it.entry.optInt("sortOrder",1000)}
 }
 fun next(ctx:Context,after:LocalDate):List<Occurrence> {
  val day=CalendarEngine.lookup(ctx,after)?:return emptyList()
  val d=data(ctx)
  val candidates=ArrayList<Occurrence>()
  for(i in 0 until d.entries.length()){
   val e=d.entries.getJSONObject(i)
   for(y in day.drYear..(day.drYear+2)){
    val o=occurrence(d,e,y)?:continue
    if(o.start.isAfter(after))candidates.add(o)
   }
  }
  val earliest=candidates.minOfOrNull{it.start}?:return emptyList()
  return candidates.filter{it.start==earliest}.sortedBy{it.entry.optInt("sortOrder",1000)}
 }
}
