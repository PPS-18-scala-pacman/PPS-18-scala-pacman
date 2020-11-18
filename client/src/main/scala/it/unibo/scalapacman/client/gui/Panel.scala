package it.unibo.scalapacman.client.gui

import java.awt.{Cursor, Font}

import javax.swing.{DefaultListCellRenderer, JButton, JLabel, JList, JPanel, JSpinner, JTextField, ListModel,
  ListSelectionModel, SpinnerModel, SpinnerNumberModel, SwingConstants}

sealed trait Panel {
  def createLabel(text: String): JLabel
  def createLabel(text: String, width: Int, height: Int): JLabel
  def createMainTitleLabel(text: String): JLabel
  def createTitleLabel(text: String): JLabel

  def createButton(text: String): JButton
  def createButton(text: String, width: Int, height: Int): JButton

  def createTextField(): JTextField

  def createJSpinner(model: SpinnerModel): JSpinner
  def createNumericJSpinner(value: Int, min: Int, max: Int): JSpinner

  def createJList[A](model: ListModel[A]): JList[A]
}

object PanelImpl {
  def apply(): PanelImpl = new PanelImpl()
}

/**
 * Rappresenta un panel generico all'interno dell'applicazione,
 * arricchita di funzioni di utilità per la creazione rapida di
 * elementi
 */
class PanelImpl extends JPanel with Panel {
  setBackground(BACKGROUND_COLOR)

  /**
   * Crea un pulsante
   * @param text il testo da visualizzare
   * @return il JButton
   */
  override def createButton(text: String): JButton = new JButton(text) {
    setFocusPainted(false)
    setContentAreaFilled(false)
    setForeground(DEFAULT_TEXT_COLOR)
    setFont(new Font(MAIN_FONT_NAME, Font.PLAIN, BUTTON_FONT_SIZE))
    setCursor(new Cursor(Cursor.HAND_CURSOR))
  }

  /**
   * Crea un pulsante di dimensioni personalizzate
   * @param text il testo da visualizzare
   * @return il JButton
   */
  override def createButton(text: String, width: Int, height: Int): JButton = {
    val button = createButton(text)
    button setSize(width, height)
    button
  }

  /**
   * Crea una label
   * @param text il testo da visualizzare
   * @return la JLabel
   */
  override def createLabel(text: String): JLabel = new JLabel(text) {
    setForeground(DEFAULT_TEXT_COLOR)
    setFont(new Font(MAIN_FONT_NAME, Font.PLAIN, LABELS_FONT_SIZE))
  }

  /**
   * Crea una label di dimensioni personalizzate
   * @param text il testo da visualizzare
   * @param width larghezza della label
   * @param height altezza della label
   * @return la JLabel
   */
  override def createLabel(text: String, width: Int, height: Int): JLabel = {
    val label = createLabel(text)
    label setSize(width, height)
    label
  }

  /**
   * Crea una label in grassetto e con font size maggiorato
   * @param text il testo da visualizzare
   * @return la JLabel
   */
  override def createMainTitleLabel(text: String): JLabel = {
    val label = createLabel(text)
    label setFont new Font(MAIN_FONT_NAME, Font.BOLD, MAIN_TITLE_LABELS_FONT_SIZE)
    label
  }

  /**
   * Crea una label in grassetto e con font size maggiorato
   * @param text il testo da visualizzare
   * @return la JLabel
   */
  override def createTitleLabel(text: String): JLabel = {
    val label = createLabel(text)
    label setFont new Font(MAIN_FONT_NAME, Font.BOLD, TITLE_LABELS_FONT_SIZE)
    label
  }

  /**
   * Crae un campo di testo generico
   * @return la JTextField
   */
  override def createTextField(): JTextField = new JTextField() {
    setFont(new Font(MAIN_FONT_NAME, Font.PLAIN, LABELS_FONT_SIZE))
  }

  /**
   * Crea uno spinner generico
   * @param model il modello da utilizzare
   * @return  lo spinner
   */
  override def createJSpinner(model: SpinnerModel): JSpinner = new JSpinner(model) {
    setFont(new Font(MAIN_FONT_NAME, Font.PLAIN, LABELS_FONT_SIZE))
  }

  /**
   * Crea uno spinner numerico
   * @param value il valore di default
   * @param min il valore minimo
   * @param max il valore massimo
   * @return  lo spinner numerico
   */
  override def createNumericJSpinner(value: Int, min: Int, max: Int): JSpinner = createJSpinner(new SpinnerNumberModel(value, min, max, 1))

  override def createJList[A](model: ListModel[A]): JList[A] = new JList(model) {
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    setFont(new Font(MAIN_FONT_NAME, Font.PLAIN, LIST_ITEM_FONT_SIZE))
    getCellRenderer.asInstanceOf[DefaultListCellRenderer] setHorizontalAlignment SwingConstants.RIGHT
  }
}
