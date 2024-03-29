package visualization

import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.bars
import org.jetbrains.kotlinx.kandy.letsplot.layers.line

object Visualizer {

    fun createAndStoreHistogram(histogram: Map<Long, Int>, outputFilePath: String) {
        plot {
            bars {
                x(histogram.keys)
                y(histogram.values)
            }
            layout.title = "Vulnerability Published Date over Time"
        }.save(outputFilePath)
    }

    fun createAndStoreLineDiagram(
        outputFilePath: String,
        name: String,
        months: List<String> = listOf(
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec"
        ),
        values: List<Long>
    ) {
        val mutableValues = values.toMutableList()
        val mutableMonths = months.toMutableList()

        fun makeListsEqual() {
            while (mutableValues.size < mutableMonths.size) {
                mutableMonths.removeLast()
            }

            while (mutableMonths.size < mutableValues.size) {
                mutableMonths.add("")
            }
        }

        makeListsEqual()
        plot {
            line {
                x(mutableMonths)
                y(mutableValues)
            }
            layout.title = name
        }.save(outputFilePath)
    }

}