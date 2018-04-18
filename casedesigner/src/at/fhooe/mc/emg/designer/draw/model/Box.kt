package at.fhooe.mc.emg.designer.draw.model

data class Box(var origin: Origin, var width: Int, var height: Int) {

    fun intersects(p: Point) : Boolean {
        return (p.x > origin.x) && (p.x < origin.x + width) && (p.y > origin.y) && (p.y < origin.y + height)
    }
}