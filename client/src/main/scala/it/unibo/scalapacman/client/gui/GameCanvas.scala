package it.unibo.scalapacman.client.gui

import java.awt.{Canvas, Color, Dimension, Graphics2D, RenderingHints}
import java.util.concurrent.Semaphore

import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.gui.GameCanvas.CompositeMessage

import scala.collection.concurrent.TrieMap
import scala.collection.immutable

object GameCanvas {
  type CompositeMessage = immutable.IndexedSeq[((Int, Int), (String, Option[ElementStyle]))]
}

class GameCanvas extends Canvas with Runnable with Logging {

  private val text: TrieMap[(Int, Int), (String, Option[ElementStyle])] = TrieMap.empty
  private var running = false
  private var gameThread: Thread = _
  private val BUFFERS_COUNT = 2
  private val pleaseRender = new Semaphore(0)

  setPreferredSize(new Dimension(WIDTH, HEIGHT))

  def setText(message: String): Unit = {
    text.clear
    setText(((0, 0), (message, None)) :: Nil toIndexedSeq)
  }

  def setText(messages: CompositeMessage): Unit = {
    text ++= messages
    if (pleaseRender.availablePermits() == 0) pleaseRender.release()
  }

  def start(): Unit =
    if (!running) {
      running = true
      gameThread = new Thread(this)
      gameThread.start()
      debug("Game thread partito")
    }

  // ends the game
  def stop(): Unit = {
    if (!running) {
      running = false
      var retry = true
      while (retry) try {
        gameThread.join()
        retry = false
        debug("Game thread fermato")
      } catch {
        case _: InterruptedException =>
          debug("Game thread non è stato fermato, riprovo tra 1 secondo")
          try Thread.sleep(1000) // scalastyle:ignore magic.number
          catch {
            case e1: InterruptedException =>
              e1.printStackTrace()
          }
      }
    }
  }

  private def render(): Unit = {
    val bs = getBufferStrategy
    val g2d = bs.getDrawGraphics.create.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    clear(g2d, 0)

    // Qui è dove inizia il rendering
    g2d.setFont(getFont)
    val metrics = g2d.getFontMetrics()
    for ((indexes, v) <- text.toSeq) {
      g2d.setColor(v._2.map(_.foregroundColor).getOrElse(Color.white))
      g2d.drawString(v._1, indexes._1 * metrics.stringWidth(v._1), (indexes._2 + 1) * (metrics.getAscent.abs + metrics.getDescent.abs))
    }

    g2d.dispose()
    bs.show()
  }

  private def clear(g2d: Graphics2D, shade: Int): Unit = {
    g2d.setColor(new Color(shade, shade, shade))
    g2d.fillRect(0, 0, WIDTH, HEIGHT)
  }

  def run(): Unit = {
    if (getBufferStrategy == null) createBufferStrategy(BUFFERS_COUNT)
    while (running) {
      render()
      pleaseRender.acquire()
      try Thread.sleep(0) // Sempre una buona idea rilasciare le risorse
      catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }
  }
}
