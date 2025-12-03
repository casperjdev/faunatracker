package com.example.faunatracker.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.readValue
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import java.io.StringReader

object Utils {
    fun csvToJson(csv: String): String {
        val reader = CSVReaderBuilder(StringReader(csv))
            .withCSVParser(
                CSVParserBuilder()
                    .withSeparator(',')       // comma separator
                    .withIgnoreQuotations(false)  // still handle quotes if present
                    .build()
            )
            .build()

        val rows = reader.readAll()

        if (rows.isEmpty()) return "[]"

        val header = rows.first()
        val dataRows = rows.drop(1)

        val jsonList = dataRows.map { row ->
            val obj = mutableMapOf<String, Any?>()

            for (i in header.indices) {
                val key = header[i]
                val value = if (i < row.size) row[i] else ""
                obj[key] = value
            }

            obj
        }

        val mapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
        return mapper.writeValueAsString(jsonList)
    }
}