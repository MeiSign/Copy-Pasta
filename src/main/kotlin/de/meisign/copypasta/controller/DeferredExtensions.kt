package de.meisign.copypasta.controller

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.springframework.web.context.request.async.DeferredResult
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
fun <T : Any?> Deferred<T>.toDeferredResult(): DeferredResult<T> {
  val result = DeferredResult<T>(TimeUnit.MINUTES.toMillis(5))
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
