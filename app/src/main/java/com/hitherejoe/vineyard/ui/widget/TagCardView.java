package com.hitherejoe.vineyard.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.leanback.widget.BaseCardView;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hitherejoe.vineyard.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TagCardView extends BaseCardView {

    @Bind(R.id.text_tag_name)
    TextView mTagNameText;

    @Bind(R.id.image_icon)
    ImageView mResultImage;

    public TagCardView(Context context, int styleResId) {
        super(new ContextThemeWrapper(context, styleResId), null, 0);
        buildLoadingCardView(styleResId);
    }

    public TagCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(getStyledContext(context, attrs, defStyleAttr), attrs, defStyleAttr);
        buildLoadingCardView(getImageCardViewStyle(context, attrs, defStyleAttr));
    }

    private void buildLoadingCardView(int styleResId) {
        setFocusable(false);
        setFocusableInTouchMode(false);
        setCardType(CARD_TYPE_MAIN_ONLY);
        setBackgroundResource(R.color.primary_light);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.view_tag_card, this);
        ButterKnife.bind(view);
        TypedArray cardAttrs =
                getContext().obtainStyledAttributes(
                        styleResId, androidx.leanback.R.styleable.lbImageCardView);
        cardAttrs.recycle();
    }

    public void setCardText(String string) {
        mTagNameText.setText(string);
    }

    public void setCardIcon(int resource) {
        mResultImage.setImageDrawable(ContextCompat.getDrawable(getContext(), resource));
    }

    private static Context getStyledContext(Context context, AttributeSet attrs, int defStyleAttr) {
        int style = getImageCardViewStyle(context, attrs, defStyleAttr);
        return new ContextThemeWrapper(context, style);
    }

    private static int getImageCardViewStyle(Context context, AttributeSet attrs, int defStyleAttr) {
        // Read style attribute defined in XML layout.
        int style = null == attrs ? 0 : attrs.getStyleAttribute();
        if (0 == style) {
            // Not found? Read global ImageCardView style from Theme attribute.
            TypedArray styledAttrs =
                    context.obtainStyledAttributes(
                            androidx.leanback.R.styleable.LeanbackTheme);
            style = styledAttrs.getResourceId(
                    androidx.leanback.R.styleable.LeanbackTheme_imageCardViewStyle, 0);
            styledAttrs.recycle();
        }
        return style;
    }

    public TagCardView(Context context) {
        this(context, null);
    }

    public TagCardView(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.leanback.R.attr.imageCardViewStyle);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

}
