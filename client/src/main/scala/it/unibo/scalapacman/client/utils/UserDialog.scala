package it.unibo.scalapacman.client.utils

import javax.swing.JOptionPane

// scalastyle:off null
object UserDialog {

  def showError(message: String): Unit = showMessageDialog(message, "Errore", JOptionPane.ERROR_MESSAGE)
  def showWarning(message: String): Unit = showMessageDialog(message, "Attenzione", JOptionPane.WARNING_MESSAGE)
  def showChoice(message: String, title: String): Int = showConfirmDialog(message, title)

  private def showMessageDialog(message: String, title: String, messageType: Int = JOptionPane.PLAIN_MESSAGE): Unit =
    JOptionPane.showMessageDialog(null, message, title, messageType)

  private def showConfirmDialog(message: String, title: String): Int = JOptionPane.showConfirmDialog(
    null,
    message,
    title,
    JOptionPane.YES_NO_OPTION,
    JOptionPane.QUESTION_MESSAGE
  )
}
// scalastyle:on null
