package com.academiaexpress.Fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.academiaexpress.Activities.ProductsActivity
import com.academiaexpress.Custom.ExpandableHeightGridView
import com.academiaexpress.Data.DeliveryMeal
import com.academiaexpress.Data.DeliveryOrder
import com.academiaexpress.R
import com.academiaexpress.Utils.AndroidUtilities
import com.academiaexpress.Utils.Image
import com.academiaexpress.Utils.LogUtil
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent

class LunchFragment : BaseProductFragment() {

    private var meal: DeliveryMeal? = null
    private var part: DeliveryOrder.OrderPart? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_lunch, container, false)
    }

    override fun animateScroll() {
        val coordinate = (AndroidUtilities.getScreenHeight(activity) - AndroidUtilities.getStatusBarHeight(context)) / 5
        smoothScroll(0, coordinate)
        Handler().postDelayed({ smoothScroll(coordinate, 0) }, 500)
    }

    private fun smoothScroll(start: Int,  end: Int) {
        (view!!.findViewById(R.id.scroll) as ScrollView).smoothScrollTo(start, end)
    }

    private fun setInfo() {
        (view!!.findViewById(R.id.textView9) as TextView).text = meal!!.mealName
        (view!!.findViewById(R.id.textView10) as TextView).text = if (meal!!.ingridients!!.isEmpty()) "" else meal!!.ingridients
        (view!!.findViewById(R.id.textView16) as TextView).text = if (meal!!.description!!.isEmpty()) "" else meal!!.description

        (view!!.findViewById(R.id.textView11) as TextView).text = Integer.toString(meal!!.price) + getString(R.string.ruble_sign)
        Image.loadDishPhoto(meal!!.photoLink, view!!.findViewById(R.id.imageView3) as ImageView)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (view == null) return

        val isCanBuyIndicator = view!!.findViewById(R.id.textViewfdfdfsfs)

        if (!meal!!.isCanBuy) isCanBuyIndicator.visibility = View.GONE
        else isCanBuyIndicator.visibility = View.VISIBLE

        isCanBuyIndicator.setOnClickListener(null)
    }

    private fun hideEnergy() {
        view!!.findViewById(R.id.en_content).visibility = View.GONE
    }

    private fun showEnergy() {
        view!!.findViewById(R.id.en_content).visibility = View.VISIBLE
    }

    private fun setEnergy(energy: Array<String>) {
        (view!!.findViewById(R.id.textView52a) as TextView).text = energy[0]
        (view!!.findViewById(R.id.textView53a) as TextView).text = energy[1]
        (view!!.findViewById(R.id.textView54a) as TextView).text = energy[2]
        (view!!.findViewById(R.id.textView55a) as TextView).text = energy[3]
    }

    private fun processEnergy() {
        val energy = meal!!.energy!!.split("energy".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()

        try {
            setEnergy(energy)
        } catch (e: Exception) {
            hideEnergy()
            LogUtil.logException(e)
        }
    }

    private fun parseEnergy() {
        if (meal!!.energy == null) {
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
        (view!!.findViewById(R.id.ingr) as LinearLayout).removeAllViews()
        if (meal!!.ingridientsList!!.size == 0) {
            view!!.findViewById(R.id.ingr).visibility = View.GONE
        } else {
            showIngredientsView()
        }
    }

    private fun showIngredientsView() {
        view!!.findViewById(R.id.ingr).visibility = View.VISIBLE
        for (i in 0..meal!!.ingridientsList!!.size - 1) {
            val v = LayoutInflater.from(context).inflate(R.layout.item_lunch_part, null)

            (v.findViewById(R.id.textView43) as TextView).text = meal!!.ingridientsList!![i].first
            (v.findViewById(R.id.textView44) as TextView).text = meal!!.ingridientsList!![i].second + getString(R.string.ingredient_postfix)

            (view!!.findViewById(R.id.ingr) as LinearLayout).addView(v)
        }
    }

    private fun initMainView() {
        val layout = view!!.findViewById(R.id.main) as FrameLayout
        val params = layout.layoutParams as LinearLayout.LayoutParams
        params.height = AndroidUtilities.getScreenHeight(activity) - AndroidUtilities.getStatusBarHeight(context)


        view!!.findViewById(R.id.textView).setOnClickListener {
            if (!DishFragment.answer) {
                Answers.getInstance().logCustom(CustomEvent(getString(R.string.event_product_added)))
            }
            DishFragment.answer = true
            (view!!.findViewById(R.id.textView) as TextView).text = getString(R.string.order_more_code)
            (activity as ProductsActivity).addProduct(part)
        }
    }

    private fun setupAdapter() {
        val adapter = object : BaseAdapter() {
            override fun getCount(): Int { return meal!!.badges!!.size }

            override fun getItem(position: Int): Any { return meal!!.badges!! }

            override fun getItemId(position: Int): Long { return position.toLong() }

            override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
                var convertView = convertView
                convertView = LayoutInflater.from(context).inflate(R.layout.item_ingredient, null)
                val name = convertView.findViewById(R.id.textView17) as TextView
                name.text = meal!!.badges!![position].second

                val icon = convertView.findViewById(R.id.imageView6) as ImageView
                icon.setPadding(AndroidUtilities.dpToPx(icon.context, 25f),
                        AndroidUtilities.dpToPx(icon.context, 25f),
                        AndroidUtilities.dpToPx(icon.context, 25f),
                        AndroidUtilities.dpToPx(icon.context, 25f))
                Image.loadPhoto(meal!!.badges!![position].first, icon)

                return convertView

            }
        }

        val gridView = view!!.findViewById(R.id.gridView2) as ExpandableHeightGridView
        gridView.isExpanded = true
        gridView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun getScrollView(): ScrollView {
        return view!!.findViewById(R.id.scroll) as ScrollView
    }

    override fun setPart(part: DeliveryOrder.OrderPart) {
        this.part = part
    }

    override fun setInfo(meal: DeliveryMeal) {
        this.meal = meal
    }
}
