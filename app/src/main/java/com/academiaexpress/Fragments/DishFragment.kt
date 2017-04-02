package com.academiaexpress.Fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.academiaexpress.Activities.ProductsActivity
import com.academiaexpress.Base.BaseProductFragment
import com.academiaexpress.Custom.ExpandableHeightGridView
import com.academiaexpress.Data.DeliveryMeal
import com.academiaexpress.Data.DeliveryOrder
import com.academiaexpress.R
import com.academiaexpress.Utils.AndroidUtilities
import com.academiaexpress.Utils.Image
import com.academiaexpress.Utils.LogUtil
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import java.util.*

class DishFragment : BaseProductFragment() {
    private val ANIMATION_DURATION = 400L

    private lateinit var meal: DeliveryMeal
    private lateinit var part: DeliveryOrder.OrderPart

    override fun animateScroll() {
        val coordinate = (AndroidUtilities.getScreenHeight(activity) - AndroidUtilities.getStatusBarHeight(context)) / 5
        smoothScroll(0, coordinate)
        Handler().postDelayed({ smoothScroll(coordinate, 0) }, ANIMATION_DURATION)
    }

    private fun smoothScroll(start: Int,  end: Int) {
        (view?.findViewById(R.id.scroll) as ScrollView).smoothScrollTo(start, end)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_product, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val outOfStockIndicator = view?.findViewById(R.id.outOfStockIndicator)
        outOfStockIndicator?.visibility = if (meal.isCanBuy) View.VISIBLE else View.GONE
        outOfStockIndicator?.setOnClickListener(null)
    }

    private fun setInfo() {
        (view?.findViewById(R.id.name) as TextView).text = meal.mealName
        (view?.findViewById(R.id.description) as TextView).text = if (meal.ingridients!!.isEmpty()) "" else meal.ingridients

        if (meal.description!!.isEmpty()) {
            (view?.findViewById(R.id.content) as TextView).text = meal.description
        } else {
            view?.findViewById(R.id.about)?.visibility = View.GONE
        }

        (view?.findViewById(R.id.price) as TextView).text = Integer.toString(meal.price) + getString(R.string.ruble_sign)
        Image.loadDishPhoto(meal.photoLink, view?.findViewById(R.id.image) as ImageView)
    }

    private fun hideEnergy() {
        if (view == null) {
            return
        }
        view!!.findViewById(R.id.en_content).visibility = View.GONE
    }

    private fun showEnergy() {
        if (view == null) {
            return
        }
        view!!.findViewById(R.id.en_content).visibility = View.VISIBLE
    }

    private fun setEnergy(energy: Array<String>) {
        (view?.findViewById(R.id.proteins) as TextView).text = energy[0]
        (view?.findViewById(R.id.fats) as TextView).text = energy[1]
        (view?.findViewById(R.id.carbohydrates) as TextView).text = energy[2]
        (view?.findViewById(R.id.nutritional_value) as TextView).text = energy[3]
    }

    private fun processEnergy() {
        if (meal.energy == null) {
            return
        }

        val energy = meal.energy!!.split("energy".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()

        try {
            setEnergy(energy)
        } catch (e: Exception) {
            hideEnergy()
            LogUtil.logException(e)
        }
    }

    private fun parseEnergy() {
        if (meal.energy == null) {
            hideEnergy()
        } else {
            showEnergy()
            processEnergy()
        }
    }

    override fun onStart() {
        super.onStart()

        setInfo()
        parseEnergy()

        val inflater = LayoutInflater.from(context)

        initFirstAdapter(inflater)
        initSecondAdapter(inflater)

        initButton()
    }

    private fun initButton() {
        view?.findViewById(R.id.order_btn)?.setOnClickListener {
            (view?.findViewById(R.id.order_btn) as TextView).text = getString(R.string.order_more_code)
            if (!answer) {
                Answers.getInstance().logCustom(CustomEvent(getString(R.string.event_product_added)))
            }
            answer = true
            (activity as ProductsActivity).addProduct(part)
        }
    }

    private fun initFirstAdapter(inflater: LayoutInflater) {
        val layout = view?.findViewById(R.id.main) as FrameLayout?
        val params = layout?.layoutParams as LinearLayout.LayoutParams?
        params?.height = AndroidUtilities.getScreenHeight(activity) - AndroidUtilities.getStatusBarHeight(context)

        val adapter = object : BaseAdapter() {
            override fun getCount(): Int {
                return if (meal.ingridientsList == null) 0 else meal.ingridientsList!!.size
            }

            override fun getItem(position: Int): Any {
                if (meal.ingridients == null) {
                    return ArrayList<String>()
                }
                return meal.ingridients!!
            }

            override fun getItemId(position: Int): Long { return position.toLong() }

            override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
                val convertView = inflater.inflate(R.layout.item_ingredient, parent)

                if (meal.ingridientsList == null) {
                    return convertView
                }

                val name = convertView.findViewById(R.id.name) as TextView
                name.text = meal.ingridientsList!![position].second

                val icon = convertView.findViewById(R.id.icon) as ImageView
                Image.loadPhoto(meal.ingridientsList!![position].first, icon)

                return convertView
            }
        }

        val view = view?.findViewById(R.id.gridView) as ExpandableHeightGridView?
        view?.isExpanded = true
        view?.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun initSecondAdapter(inflater: LayoutInflater) {
        val adapter = object : BaseAdapter() {
            override fun getCount(): Int { return meal.badges!!.size }

            override fun getItem(position: Int): Any {
                if (meal.badges == null) {
                    return ArrayList<Any>()
                } else {
                    return meal.badges!!
                }
            }

            override fun getItemId(position: Int): Long { return position.toLong() }

            override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
                var convertView = convertView
                convertView = inflater.inflate(R.layout.item_ingredient, null)

                if (meal.badges == null) {
                    return convertView
                }

                val name = convertView.findViewById(R.id.name) as TextView
                name.text = meal.badges!![position].second
                val padding = AndroidUtilities.dpToPx(name.context, 25f)
                val icon = convertView.findViewById(R.id.icon) as ImageView
                icon.setPadding(padding, padding, padding, padding)

                Image.loadPhoto(meal.badges!![position].first, icon)

                return convertView

            }
        }

        val view = view?.findViewById(R.id.gridView2) as ExpandableHeightGridView?
        view?.isExpanded = true
        view?.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun getScrollView(): ScrollView? {
        return view?.findViewById(R.id.scroll) as ScrollView?
    }

    override fun setPart(part: DeliveryOrder.OrderPart) {
        this.part = part
    }

    override fun setInfo(meal: DeliveryMeal) {
        this.meal = meal
    }

    companion object {
        var answer = false
    }
}
