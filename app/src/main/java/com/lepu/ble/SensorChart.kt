package com.lepu.ble

import com.juul.krayon.axis.axisBottom
import com.juul.krayon.axis.axisLeft
import com.juul.krayon.axis.call
import com.juul.krayon.color.steelBlue
import com.juul.krayon.color.white
import com.juul.krayon.element.GroupElement
import com.juul.krayon.element.PathElement
import com.juul.krayon.element.RootElement
import com.juul.krayon.element.TransformElement
import com.juul.krayon.element.withKind
import com.juul.krayon.kanvas.Paint
import com.juul.krayon.kanvas.Transform
import com.juul.krayon.scale.domain
import com.juul.krayon.scale.extent
import com.juul.krayon.scale.range
import com.juul.krayon.scale.scale
import com.juul.krayon.selection.append
import com.juul.krayon.selection.asSelection
import com.juul.krayon.selection.data
import com.juul.krayon.selection.each
import com.juul.krayon.selection.join
import com.juul.krayon.selection.selectAll
import com.juul.krayon.shape.line

data class Sample(val t: Float, val x: Float)

private val xLinePaint = Paint.Stroke(steelBlue, 1f)

internal fun chart(root: RootElement, width: Float, height: Float, data: List<Sample>) {
    try {
        val leftMargin = 60f
        val topMargin = 20f
        val rightMargin = 20f
        val bottomMargin = 40f

        val innerWidth = width - leftMargin - rightMargin
        val innerHeight = height - topMargin - bottomMargin

        val x = scale()
            .domain(data.extent { it.t })
            .range(0f, innerWidth)

        val y = scale()
            .domain(listOf(0f, 255f))
            .range(innerHeight, 0f)


        val body = root.asSelection()
            .selectAll(TransformElement.withKind("body"))
            .data(listOf(null))
            .join { append(TransformElement).each { kind = "body" } }
            .each {
                transform = Transform.Translate(
                    horizontal = leftMargin,
                    vertical = topMargin
                )
            }


        body.selectAll(TransformElement.withKind("x-axis"))
            .data(listOf(null))
            .join { append(TransformElement).each { kind = "x-axis" } }
            .each { transform = Transform.Translate(vertical = innerHeight) }
            .call(axisBottom(x).apply {
                lineColor = white
                textColor = white
            })


        body.selectAll(GroupElement.withKind("y-axis"))
            .data(listOf(null))
            .join { append(GroupElement).each { kind = "y-axis" } }
            .call(axisLeft(y).apply {
                lineColor = white
                textColor = white
            })


        val xLine = line<Sample>()
            .x { (p) -> x.scale(p.t) }
            .y { (p) -> y.scale(p.x) }



        body.selectAll(PathElement.withKind("x-line"))
            .data(listOf(data, data))
            .join {
                append(PathElement).each {
                    kind = "x-line"
                    paint = xLinePaint
                }
            }.each { (d) ->
                path = xLine.render(d)
            }
    } catch (e: Exception) {
        e.printStackTrace()
    }

}
