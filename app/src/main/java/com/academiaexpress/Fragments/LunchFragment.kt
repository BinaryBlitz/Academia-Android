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
import com.academiaexpress.Data.Meal
import com.academiaexpress.Data.Order
import com.academiaexpress.R
import com.academiaexpress.Utils.AndroidUtilities
import com.academiaexpress.Utils.Image
import com.academiaexpress.Utils.LogUtil
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import java.util.*

class LunchFragment : BaseProductFragment() {
    private val ANIMATION_DURATION = 400L

    private lateinit var meal: Meal
    private lateinit var part: Order.OrderPart

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_lunch, container, false)
    }

    override fun animateScroll() {
        val coordinate = (AndroidUtilities.getScreenHeight(activity) - AndroidUtilities.getStatusBarHeight(context)) / 5
        Handler().post({ smoothScroll(0, coordinate) })
        Handler().postDelayed({ smoothScroll(coordinate, 0) }, ANIMATION_DURATION)
    }

    private fun smoothScroll(start: Int,  end: Int) {
        (view?.findViewById(R.id.scroll) as ScrollView?)?.smoothScrollTo(start, end)
    }

    private fun setInfo() {
        (view?.findViewById(R.id.name) as TextView?)?.text = meal.mealName
        (view?.findViewById(R.id.description) as TextView?)?.text = if (meal.ingridients == null) "" else meal.ingridients

        if (meal.description!!.isEmpty()) {
            (view?.findViewById(R.id.content) as TextView?)?.text = meal.description
        } else {
            view?.findViewById(R.id.about)?.visibility = View.GONE
        }

        (view?.findViewById(R.id.price) as TextView?)?.text = Integer.toString(meal.price) + getString(R.string.ruble_sign)

        Image.loadDishPhoto(meal.photoLink, view?.findViewById(R.id.image) as ImageView?)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val outOfStockIndicator = view?.findViewById(R.id.outOfStockIndicator)
        outOfStockIndicator?.visibility = if (meal.isCanBuy) View.VISIBLE else View.GONE
        outOfStockIndicator?.setOnClickListener(null)
    }

    private fun hideEnergy() {
        view?.findViewById(R.id.en_content)?.visibility = View.GONE
    }

    private fun showEnergy() {
        view?.findViewById(R.id.en_content)?.visibility = View.VISIBLE
    }

    private fun setEnergy(energy: Array<String>) {
        (view?.findViewById(R.id.proteins) as TextView?)?.text = energy[0]
        (view?.findViewById(R.id.fats) as TextView?)?.text = energy[1]
        (view?.findViewById(R.id.carbohydrates) as TextView?)?.text = energy[2]
        (view?.findViewById(R.id.nutritional_value) as TextView?)?.text = energy[3]
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
        initMainView()
        setupAdapter()
        initIngredientsView()
    }

    private fun initIngredientsView() {
        if (meal.ingridientsList == null) {
            return
        }

        (view?.findViewById(R.id.ingr) as LinearLayout?)?.removeAllViews()
        if (meal.ingridientsList!!.size == 0) {
            view?.findViewById(R.id.ingr)?.visibility = View.GONE
        } else {
            showIngredientsView()
        }
    }

    private fun showIngredientsView() {
        if (meal.ingridientsList == null) {
            return
        }

        view?.findViewById(R.id.ingr)?.visibility = View.VISIBLE
        for (i in 0..meal.ingridientsList!!.size - 1) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_lunch_part, null)

            (view.findViewById(R.id.name) as TextView).text = meal.ingridientsList!![i].first
            (view.findViewById(R.id.price) as TextView).text = meal.ingridientsList!![i].second + getString(R.string.ingredient_postfix)

            (view?.findViewById(R.id.ingr) as LinearLayout?)?.addView(view)
        }
    }

    private fun initMainView() {
        val layout = view?.findViewById(R.id.main) as FrameLayout?
        val params = layout?.layoutParams as LinearLayout.LayoutParams?
        params?.height = AndroidUtilities.getScreenHeight(activity) - AndroidUtilities.getStatusBarHeight(context)

        view?.findViewById(R.id.make_order_btn)?.setOnClickListener {
            if (!DishFragment.answer) {
                Answers.getInstance().logCustom(CustomEvent(getString(R.string.event_product_added)))
            }
            DishFragment.answer = true
            (view?.findViewById(R.id.make_order_btn) as TextView?)?.text = getString(R.string.order_more_code)
            (activity as ProductsActivity).addProduct(part)
        }
    }

    private fun setupAdapter() {
        val adapter = object : BaseAdapter() {
            override fun getCount(): Int {
                if (meal.badges == null) {
                    return 0
                }
                return meal.badges!!.size
            }

            override fun getItem(position: Int): Any {
                if (meal.badges == null) {
                    return ArrayList<String>()
                }
                return meal.badges!!
            }

            override fun getItemId(position: Int): Long { return position.toLong() }

            override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
                var convertView = convertView
                convertView = LayoutInflater.from(context).inflate(R.layout.item_ingredient, null)

                if (meal!!.badges == null) {
                    return convertView
                }

                val name = convertView.findViewById(R.id.name) as TextView
                name.text = meal.badges!![position].second

                val icon = convertView.findViewById(R.id.image) as ImageView
                val padding = AndroidUtilities.dpToPx(icon.context, 25f)

                icon.setPadding(padding, padding, padding, padding)
                Image.loadPhoto(meal.badges!![position].first, icon)

                return convertView

            }
        }

        val gridView = view?.findViewById(R.id.gridView) as ExpandableHeightGridView?
        gridView?.isExpanded = true
        gridView?.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun getScrollView(): ScrollView? {
        return view!!.findViewById(R.id.scroll) as ScrollView?
    }

    override fun setPart(part: Order.OrderPart) {
        this.part = part
    }

    override fun setInfo(meal: Meal) {
        this.meal = meal
    }
}
