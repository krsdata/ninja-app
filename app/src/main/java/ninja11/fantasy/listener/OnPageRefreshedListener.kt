package ninja11.fantasy.listener

interface OnPageRefreshedListener {
    fun onPageCreated(pageName:String)
    fun onRefreshed(pageName:String)
}