package com.github.quarck.qrckwatch.weather

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList

import android.util.Log

fun NodeList?.toTypedList(): List<Node>? {
    if (this == null)
        return null

    val ret = mutableListOf<Node>()

    for (i in 0..this.length) {
        val item = this.item(i)
        if (item != null)
            ret.add(item)
    }


    return ret
}

object Parser {

    @SuppressWarnings("deprecation")

    @JvmStatic
    fun parse(rssXml: String): Weather? {

        val ret = Weather()

        val inputStringBytes = ByteArrayInputStream(rssXml.toByteArray())

        val builderFactory = DocumentBuilderFactory.newInstance()
        //val builder: DocumentBuilder

        try {
            val builder = builderFactory.newDocumentBuilder()
            val document = builder.parse(inputStringBytes)

            val rootNodes = document.childNodes

            val results =
                    rootNodes?.toTypedList()
                            ?.find { it.nodeName == "query" }
                            ?.childNodes?.toTypedList()
                            ?.find { it.nodeName == "results" }
                            ?.childNodes?.toTypedList()
                            ?.find { it.nodeName == "channel" }
                            ?.childNodes?.toTypedList()

            if (results != null) {

                for (item in results) {

                    when (item.nodeName) {

                        "yweather:units" -> parseUnits(item, ret)
                        "yweather:location" -> parseLocation(item, ret)
                        "yweather:wind" -> parseWind(item, ret)
                        "yweather:atmosphere" -> parseAtmosphere(item, ret)
                        "yweather:astronomy" -> parseAstronomy(item, ret)
                        "item" -> parseItem(item, ret)
                    }

                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        if (ret.forecasts.size > 0)
            ret.forecast = ret.forecasts.sortedBy { it.date.time }.firstOrNull()

        Log.w("WEATHER", "Returning: " + ret.forecast.toString())

        return ret
    }

    private fun parseItem(item: Node, ret: Weather) {

        val childNodes = item.childNodes?.toTypedList() ?: return

        for (itemChild in childNodes) {

            if (itemChild.nodeName == "yweather:condition") {
                val attrs = itemChild.attributes

                val text = attrs.getNamedItem("text")
                val code = attrs.getNamedItem("code")
                val temp = attrs.getNamedItem("temp")

                if (text != null) {
                    Log.d("WEATHER", "text = " + text.nodeValue)
                    try {
                        ret!!.currentCondition = text.nodeValue
                    } catch (ex: Exception) {
                    }

                }
                if (code != null) {
                    Log.d("WEATHER", "code = " + code.nodeValue)
                    try {
                        ret!!.currentCode = Integer.valueOf(code.nodeValue)!!
                    } catch (ex: Exception) {
                        ret!!.currentCode = 0
                    }
                }
                if (temp != null) {
                    Log.d("WEATHER", "temp = " + temp.nodeValue)
                    try {
                        ret!!.currentTemp = java.lang.Float.valueOf(temp.nodeValue)!!
                    } catch (ex: Exception) {
                        ret!!.currentTemp = 0f
                    }

                }
            } else if (itemChild.nodeName == "yweather:forecast") {
                val attrs = itemChild.attributes

                // parse forecast
                val text = attrs.getNamedItem("text")
                val code = attrs.getNamedItem("code")
                val low = attrs.getNamedItem("low")
                val high = attrs.getNamedItem("high")
                val date = attrs.getNamedItem("date")

                val df = Weather.DayForecast()

                if (text != null) {
                    Log.d("WEATHER", "text = " + text.nodeValue)
                    df.conditionText = text.nodeValue
                }
                if (code != null) {
                    Log.d("WEATHER", "code = " + code.nodeValue)
                    try {
                        df.code = Integer.valueOf(code.nodeValue)!!
                    } catch (ex: Exception) {
                        df.code = 0
                    }

                }
                if (low != null) {
                    Log.d("WEATHER", "low = " + low.nodeValue)
                    try {
                        df.tempLow = java.lang.Float.valueOf(low.nodeValue)!!
                    } catch (ex: Exception) {
                        df.tempLow = 0f
                    }

                }
                if (high != null) {
                    Log.d("WEATHER", "high = " + high.nodeValue)
                    try {
                        df.tempHigh = java.lang.Float.valueOf(high.nodeValue)!!
                    } catch (ex: Exception) {
                        df.tempHigh = 0f
                    }

                }
                if (date != null) {
                    Log.d("WEATHER", "date = " + date.nodeValue)
                    try {
                        df.date = ParseDate(date.nodeValue)
                    } catch (ex: Exception) {
                        df.date = null
                    }

                }

                ret!!.forecasts.add(df)
            }
        }
    }

    private fun parseAstronomy(item: Node, ret: Weather) {
        // ignore
        // example:
        // <yweather:astronomy xmlns:yweather="http://xml.weather.yahoo.com/ns/rss/1.0" sunrise="5:16 am" sunset="9:46 pm"/>
    }

    private fun parseAtmosphere(item: Node, ret: Weather) {
        val attrs = item.attributes

        val humidity = attrs.getNamedItem("humidity")
        val visibility = attrs.getNamedItem("visibility")
        val pressure = attrs.getNamedItem("pressure")
        val rising = attrs.getNamedItem("rising")

        if (humidity != null) {
            Log.d("WEATHER", "humidity = " + humidity.nodeValue)
            try {
                ret!!.humidity = java.lang.Float.valueOf(humidity.nodeValue)!!
            } catch (ex: Exception) {
                ret!!.humidity = 0f
            }

        }
        if (visibility != null) {
            Log.d("WEATHER", "visibility = " + visibility.nodeValue)
            try {
                ret!!.visibility = java.lang.Float.valueOf(visibility.nodeValue)!!
            } catch (ex: Exception) {
                ret!!.visibility = 0f
            }

        }
        if (pressure != null) {
            Log.d("WEATHER", "pressure = " + pressure.nodeValue)
            try {
                ret!!.pressure = java.lang.Float.valueOf(pressure.nodeValue)!!
            } catch (ex: Exception) {
                ret!!.pressure = 0f
            }

        }
        if (rising != null) {
            Log.d("WEATHER", "rising = " + rising.nodeValue)
            try {
                ret!!.rising = Integer.valueOf(rising.nodeValue)!!
            } catch (ex: Exception) {
                ret!!.rising = 0
            }

        }

    }

    private fun parseWind(item: Node, ret: Weather) {
        val attrs = item.attributes

        val chill = attrs.getNamedItem("chill")
        val direction = attrs.getNamedItem("direction")
        val speed = attrs.getNamedItem("speed")

        if (chill != null) {
            Log.d("WEATHER", "chill = " + chill.nodeValue)
            try {
                ret!!.windChill = java.lang.Float.valueOf(chill.nodeValue)!!
            } catch (ex: Exception) {
                ret!!.windChill = 0f
            }

        }
        if (direction != null) {
            Log.d("WEATHER", "direction = " + direction.nodeValue)
            try {
                ret!!.windDirection = java.lang.Float.valueOf(direction.nodeValue)!!
            } catch (ex: Exception) {
                ret!!.windDirection = 0f
            }

        }
        if (speed != null) {
            Log.d("WEATHER", "speed = " + speed.nodeValue)
            try {
                ret!!.windSpeed = java.lang.Float.valueOf(speed.nodeValue)!!
            } catch (ex: Exception) {
                ret!!.windSpeed = 0f
            }

        }
    }

    private fun parseLocation(item: Node, ret: Weather) {
        val attrs = item.attributes

        val city = attrs.getNamedItem("city")
        val country = attrs.getNamedItem("country")

        if (city != null) {
            Log.d("WEATHER", "city = " + city.nodeValue)
            ret!!.city = city.nodeValue
        }
        if (country != null) {
            Log.d("WEATHER", "country = " + country.nodeValue)
            ret!!.country = country.nodeValue
        }
    }

    private fun parseUnits(item: Node, ret: Weather) {
        val attrs = item.attributes

        val distance = attrs.getNamedItem("distance")
        val pressure = attrs.getNamedItem("pressure")
        val speed = attrs.getNamedItem("speed")
        val temperature = attrs.getNamedItem("temperature")

        if (distance != null && distance.nodeValue != "km")
            throw Exception("Unknown distance unit")

        if (pressure != null && pressure.nodeValue != "mb")
            throw Exception("Unknown pressure unit")

        if (speed != null && speed.nodeValue != "km/h")
            throw Exception("Unknown speed unit")

        if (temperature != null && temperature.nodeValue != "C")
            throw Exception("Unknown temperature unit")
    }


    private fun ParseDate(strDate: String): Date? {
        val df = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)
        val result: Date?
        try {
            result = df.parse(strDate)

            Log.d("WEATHER", "Parsed date: " + result!!)
        } catch (e: ParseException) {
            e.printStackTrace()
            result = null
        }

        return result
    }
}
