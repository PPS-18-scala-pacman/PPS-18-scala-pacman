package it.unibo.scalapacman.client.controller

import java.util.concurrent.Semaphore

import grizzled.slf4j.Logging
import it.unibo.scalapacman.common.{JSONConverter, UpdateModelDTO}

/**
 * Rappresenta l'entità che gestisce la ricezione dell'aggiornamento della partita dal server
 * sotto forma di JSON ed esegue le operazioni di conversione da passare poi agli atri componenti.
 * Implementa un Runnable in modo da eseguire le operazioni di conversione del JSON in un thread separato,
 * per non appesantire il thread principale
 * @param notifyModelUpdate funzione a cui viene passato l'aggiornamento della partita convertito
 */
class WebSocketConsumer(notifyModelUpdate: UpdateModelDTO => Unit) extends Runnable with Logging {
  val semaphore = new Semaphore(0)
  private var message: Option[String] = None
  private var running = true

  def addMessage(msg: String): Unit = this.synchronized {
    message = Some(msg)
    if (semaphore.availablePermits() == 0) semaphore.release()
  }

  private def getMessage: Option[UpdateModelDTO] = this.synchronized {
    semaphore.acquire()
    message flatMap(JSONConverter.fromJSON[UpdateModelDTO](_))
  }

  def terminate(): Unit = running = false

  override def run(): Unit = {
    while (running) {
      getMessage match {
        case None => error("Aggiornamento dati dal server non valido")
        case Some(model) => /*debug(model);*/ notifyModelUpdate(model)
      }
      try Thread.sleep(0) // Sempre una buona idea rilasciare le risorse
      catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }
  }
}
