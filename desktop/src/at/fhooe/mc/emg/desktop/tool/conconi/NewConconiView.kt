package at.fhooe.mc.emg.desktop.tool.conconi

class NewConconiView { //: ConconiToolView {

/*
    private var chartAverage: XYChart? = null
    private var chartAverageWrapper: XChartPanel<XYChart>? = null

    private var viewCallback: ConconiToolViewCallback? = null

    private var xVals: MutableList<Double> = mutableListOf()
    private var yAvg: MutableList<Double> = mutableListOf()
    private var yHr: MutableList<Int> = mutableListOf()

    private val errorHandler = Consumer<Throwable> { throwable -> JOptionPane.showMessageDialog(panelMain, throwable.localizedMessage) }

    private val actionListener = ActionListener { e ->
        when {
            e.source === btnStart -> {
                viewCallback?.onStartClicked()
                updateButtonStates(false)
            }
            e.source === btnStop -> {
                viewCallback?.onStopClicked()
                labelTime.setText("Test finished!")
                updateButtonStates(true)
            }
            e.source === btnSave -> {
                val saveFileName = UiUtils.showConconiSaveDialog()
                viewCallback?.onSaveClicked(saveFileName, errorHandler)
            }
            e.source === btnLoad -> {
                val loadFileName = UiUtils.showConconiLoadDialog()
                viewCallback?.onLoadClicked(loadFileName, errorHandler)
            }
        }
    }



    override fun onCountdownTick(seconds: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTick(seconds: Int, goal: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRoundDataAvailable(data: ConconiRoundData, round: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPlayCountdownSound() {
        val file = File(System.getProperty("user.dir") + "/data/sound/conconi_countdown.wav")
        DesktopUtils.playSound(file)
    }

    override fun setup(toolViewCallback: ConconiToolViewCallback, showViewImmediate: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showView() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // ------------------------------------------------

    private fun wrap(): JFrame {
        val frame = JFrame()
        frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        frame.title = "Conconi Test"
        frame.iconImage = Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_tool_conconi.png")
        frame.contentPane = panelMain
        frame.setBounds(650, 100, 450, 500)
        return frame
    }
    */

}