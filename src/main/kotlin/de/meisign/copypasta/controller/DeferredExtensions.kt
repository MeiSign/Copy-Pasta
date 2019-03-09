package de.meisign.copypasta.controller

import kotlinx.coroutines.Deferred
import org.springframework.web.context.request.async.DeferredResult

fun <T : Any?> Deferred<T>.toDeferredResult(): DeferredResult<T> {
  val result = DeferredResult<T>()
  this.invokeOnCompletion {
    exception ->
    if (exception != null) {
      result.setErrorResult(exception)
    } else {
      result.setResult(this.getCompleted())
    }
  }

  return result
}
