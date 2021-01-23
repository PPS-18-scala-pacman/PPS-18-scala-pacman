package it.unibo.scalapacman.client.gui

import java.awt.{Color, Dimension, Graphics}

import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.gui.GameCanvas.CompositeMessage
import javax.swing.JPanel

object GameCanvas {
  type CompositeMessage = Map[(Int, Int), (String, Option[ResolvedElementStyle])]
}

/**
 * Oggetto sul quale viene disegnata la mappa del gioco, utilizzato in PlayView.
 * Implementato come thread separato per non pesare sull'esecuzione del thread principale
 * con il rischio di limitare l'esperienza dell'utente durante l'uso dell'interfaccia
 */
class GameCanvas extends JPanel with Logging {

  private var bufferedText: CompositeMessage = Map.empty
  private var shouldCallRepaint: Boolean = true

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
  def setText(messages: CompositeMessage): Unit = this.synchronized {
    bufferedText = messages
    if (shouldCallRepaint) repaint()
    shouldCallRepaint = false
  }

  def getText: CompositeMessage = this.synchronized {
    shouldCallRepaint = true
    bufferedText
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)

    val graphics = g.create()
    val textToPaint = getText

    // Rendering del valore attuale di text sul canvas
    val metrics = graphics.getFontMetrics()
    for ((indexes, v) <- textToPaint) {
      graphics.setColor(v._2.map(_.foregroundColor).getOrElse(Color.white))
      graphics.drawString(v._1, indexes._1 * metrics.stringWidth(v._1), (indexes._2 + 1) * (metrics.getAscent.abs + metrics.getDescent.abs))
    }

    graphics.dispose()
  }
}
