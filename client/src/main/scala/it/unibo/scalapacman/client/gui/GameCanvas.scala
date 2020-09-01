package it.unibo.scalapacman.client.gui

import java.awt.{Color, Dimension, Graphics}
import java.util.concurrent.Semaphore

import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.gui.GameCanvas.CompositeMessage
import javax.swing.{JPanel, SwingUtilities}

object GameCanvas {
  type CompositeMessage = Map[(Int, Int), (String, Option[ElementStyle])]
}

class GameCanvas extends JPanel with Runnable with Logging {

  private var text: Map[(Int, Int), (String, Option[ElementStyle])] = Map.empty
  private var running = false
  private var gameThread: Thread = _
  private val pleaseRender = new Semaphore(0)

  setPreferredSize(new Dimension(WIDTH, HEIGHT))
  setIgnoreRepaint(true)

  def setText(message: String): Unit = setText(Map((0, 0) -> (message, None)))

  def setText(messages: CompositeMessage): Unit = {
    text = messages
    if (!running) repaint()

    if (pleaseRender.availablePermits() == 0) pleaseRender.release()
  }

  // Inizia il thread
  def start(): Unit =
    if (!running) {
      running = true
      gameThread = new Thread(this)
      gameThread.start()
      debug("Game thread partito")
    }

  // Termina il thread
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
//      debug("Ask repaint " + text.size)
      val that = this
      SwingUtilities.invokeAndWait(() => that.repaint())
      pleaseRender.acquire()
    }
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)

    val graphics = g.create()
    val textToPaint = text
//    debug("painting " + textToPaint.size)

    // Qui è dove inizia il rendering
    val metrics = graphics.getFontMetrics()
    for ((indexes, v) <- textToPaint) {
      graphics.setColor(v._2.map(_.foregroundColor).getOrElse(Color.white))
      graphics.drawString(v._1, indexes._1 * metrics.stringWidth(v._1), (indexes._2 + 1) * (metrics.getAscent.abs + metrics.getDescent.abs))
    }

    graphics.dispose()
  }
}
