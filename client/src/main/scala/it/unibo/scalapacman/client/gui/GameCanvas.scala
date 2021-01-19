package it.unibo.scalapacman.client.gui

import java.awt.{Color, Dimension, Graphics}
import java.util.concurrent.Semaphore

import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.gui.GameCanvas.CompositeMessage
import javax.swing.{JPanel, SwingUtilities}

object GameCanvas {
  type CompositeMessage = Map[(Int, Int), (String, Option[ResolvedElementStyle])]
}

/**
 * Oggetto sul quale viene disegnata la mappa del gioco, utilizzato in PlayView.
 * Implementato come thread separato per non pesare sull'esecuzione del thread principale
 * con il rischio di limitare l'esperienza dell'utente durante l'uso dell'interfaccia
 */
class GameCanvas extends JPanel with Runnable with Logging {

  private var text: Map[(Int, Int), (String, Option[ResolvedElementStyle])] = Map.empty
  private var running = false
  private var gameThread: Thread = _
  private val pleaseRender = new Semaphore(0)

  setPreferredSize(new Dimension(WIDTH, HEIGHT))
  setIgnoreRepaint(true)

  /**
   * Imposta un messaggio da mostrare sul canvas
   *
   * @param message il messaggio da mostrare
   */
  def setText(message: String): Unit = setText(Map((0, 0) -> (message, None)))

  /**
   * Imposta il testo da disegnare sul canvas.
   * Le informazioni all'interno dell'oggetto messages fanno riferimento
   * alla posizione dove disegnare ogni singolo carattere accompagnate
   * dall'eventuale stile da assegnarli.
   *
   * Se il thread attuale non è in funzione (running è a false), allora il messaggio
   * viene stampato immediatamente, altrimenti viene rispettata l'attesa del semaforo
   *
   * @param messages il messaggio da mostrare
   */
  def setText(messages: CompositeMessage): Unit = {
    text = messages
    if (!running) repaint()

    if (pleaseRender.availablePermits() == 0) pleaseRender.release()
  }

  /**
   * Attiva l'esecuzione del thread per disegnare sul canvas
   */
  def start(): Unit =
    if (!running) {
      running = true
      gameThread = new Thread(this)
      gameThread.start()
      debug("Game thread partito")
    }

  /**
   * Termina il thread per disegnare sul canvas
   */
  def stop(): Unit = {
    if (running) {
      running = false
      var retry = true
      while (retry) try {
        // Mi assicuro che il semaforo non tenga bloccato il thread
        pleaseRender.release()
        gameThread.join()
        retry = false
        debug("Game thread fermato")
      } catch {
        case _: InterruptedException =>
          debug("Game thread non è stato fermato, riprovo tra 1 secondo")
          try Thread.sleep(1000) // scalastyle:ignore magic.number
          catch {
            case e: InterruptedException =>
              e.printStackTrace()
          }
      }
    }
  }

  def run(): Unit = {
    pleaseRender.tryAcquire()
    while (running) {
      val that = this
      SwingUtilities.invokeAndWait(() => that.repaint())
      pleaseRender.acquire()
    }
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)

    val graphics = g.create()
    val textToPaint = text

    // Rendering del valore attuale di text sul canvas
    val metrics = graphics.getFontMetrics()
    for ((indexes, v) <- textToPaint) {
      graphics.setColor(v._2.map(_.foregroundColor).getOrElse(Color.white))
      graphics.drawString(v._1, indexes._1 * metrics.stringWidth(v._1), (indexes._2 + 1) * (metrics.getAscent.abs + metrics.getDescent.abs))
    }

    graphics.dispose()
  }
}
