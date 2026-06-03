package com.example.moonbrewtavern.ui.common

import android.util.Log
import com.example.moonbrewtavern.domain.model.VisitorDefinition

private const val VisitorUiFallbackTag = "VisitorUiFallback"

/** Returns the matching visitor definition or logs a warning when the catalog is inconsistent. */
fun resolveVisitorDefinition(
  visitorId: String,
  visitorDefinitions: Map<String, VisitorDefinition>,
): VisitorDefinition? {
  val definition = visitorDefinitions[visitorId]
  if (definition != null) {
    return definition
  }

  Log.w(VisitorUiFallbackTag, "Missing VisitorDefinition for visitorId=$visitorId. Skipping render.")
  return null
}
