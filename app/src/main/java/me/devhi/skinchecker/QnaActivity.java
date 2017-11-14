package me.devhi.skinchecker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.tech.freak.wizardpager.model.AbstractWizardModel;
import com.tech.freak.wizardpager.model.ModelCallbacks;
import com.tech.freak.wizardpager.model.Page;
import com.tech.freak.wizardpager.ui.PageFragmentCallbacks;
import com.tech.freak.wizardpager.ui.ReviewFragment;
import com.tech.freak.wizardpager.ui.StepPagerStrip;

import java.util.HashMap;
import java.util.List;

import static com.tech.freak.wizardpager.model.Page.SIMPLE_DATA_KEY;

public class QnaActivity extends AppCompatActivity implements
        PageFragmentCallbacks, ReviewFragment.Callbacks, ModelCallbacks {
    public final static int SUCCESS_CALL_QNA = 1000;
    public final static int FAILD_CALL_QNA = 9999;

    HashMap<Integer, Integer> qnaData = new HashMap<>();

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    private boolean mEditingAfterReview;

    private AbstractWizardModel mWizardModel = new SkinQnAWizardModel(this);

    private boolean mConsumePageSelectedEvent;

    private Button mNextButton;
    private Button mPrevButton;

    private List<Page> mCurrentPageSequence;
    private StepPagerStrip mStepPagerStrip;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qna);

        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            mWizardModel.load(savedInstanceState.getBundle("model"));
        }

        mWizardModel.registerListener(this);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mStepPagerStrip = (StepPagerStrip) findViewById(R.id.strip);
        mStepPagerStrip
                .setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
                    @Override
                    public void onPageStripSelected(int position) {
                        position = Math.min(mPagerAdapter.getCount() - 1,
                                position);
                        if (mPager.getCurrentItem() != position) {
                            mPager.setCurrentItem(position);
                        }
                    }
                });

        mNextButton = (Button) findViewById(R.id.next_button);
        mPrevButton = (Button) findViewById(R.id.prev_button);

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mStepPagerStrip.setCurrentPage(position);

                if (mConsumePageSelectedEvent) {
                    mConsumePageSelectedEvent = false;
                    return;
                }

                mEditingAfterReview = false;
                updateBottomBar();
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mPager.getCurrentItem() == mCurrentPageSequence.size()) {
                        analysisQna();
                    } else {
                        if (mEditingAfterReview) {
                            mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
                        } else {
                            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                        }
                    }
                }
            });

            mPrevButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                }
            });

            onPageTreeChanged();
            updateBottomBar();
        }

        @Override
        public void onPageTreeChanged() {
            mCurrentPageSequence = mWizardModel.getCurrentPageSequence();
            recalculateCutOffPage();
            mStepPagerStrip.setPageCount(mCurrentPageSequence.size() + 1); // + 1 =
            // review
            // step
            mPagerAdapter.notifyDataSetChanged();
            updateBottomBar();
        }

    private void updateBottomBar() {
        int position = mPager.getCurrentItem();
        if (position == mCurrentPageSequence.size()) {
            mNextButton.setText(R.string.finish);
            mNextButton.setBackgroundResource(R.drawable.finish_background);
            mNextButton.setTextAppearance(this, R.style.TextAppearanceFinish);
        } else {
            mNextButton.setText(mEditingAfterReview ? R.string.review
                    : R.string.next);
            mNextButton
                    .setBackgroundResource(R.drawable.selectable_item_background);
            TypedValue v = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v,
                    true);
            mNextButton.setTextAppearance(this, v.resourceId);
            mNextButton.setEnabled(position != mPagerAdapter.getCutOffPage());
        }

        mPrevButton
                .setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWizardModel.unregisterListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("model", mWizardModel.save());
    }

    @Override
    public AbstractWizardModel onGetModel() {
        return mWizardModel;
    }

    @Override
    public void onEditScreenAfterReview(String key) {
        for (int i = mCurrentPageSequence.size() - 1; i >= 0; i--) {
            if (mCurrentPageSequence.get(i).getKey().equals(key)) {
                mConsumePageSelectedEvent = true;
                mEditingAfterReview = true;
                mPager.setCurrentItem(i);
                updateBottomBar();
                break;
            }
        }
    }

    protected int keyToIndex(String key) {
        switch (key) {
            case "세안 후 아무것도 안 바르면 피부가 땅기는 느낌이 들어요.":
                return 0;
            case "날씨가 건조하면 각질이 일어나는 편이에요.":
                return 1;
            case "보습제를 발라도 3~4시간이 지나면 피부가 땅기거나 푸석푸석해요.":
                return 2;
            case "오후가 되면 이마와 볼 부분이 번들거려요.":
                return 3;
            case "이마와 코(T존) 부위의 모공이 크고 도드라져요.":
                return 4;
            case "코와 턱 주위에 블렉헤드나 여드름이 자주 생겨요.":
                return 5;
            case "화장품을 바꾸면 트러블이 생겨요.":
                return 6;
            case "스트레스를 받거나 과음했을 때 뾰루지가 나요.":
                return 7;
            case "자외선, 미세먼지, 환절기 등 외부 요인에 따라 피부가 가렵거나 따가울 때가 있어요.":
                return 8;
            case "피부가 얇고 탄력이 없는 편이에요.":
                return 9;
            case "눈가나 입가에 잔주름이 있어요.":
                return 10;
            case "턱선이나 얼굴 살이 처진 느낌이에요.":
                return 11;
        }
        return -1;
    }

    protected int valueToIndex(String value) {
        switch (value) {
            case "매우 아니다":
                return 1;
            case "아니다":
                return 2;
            case "보통이다":
                return 3;
            case "그렇다":
                return 4;
            case "매우 그렇다":
                return 5;
        }
        return -1;
    }

    protected void analysisQna() {
        int water = (qnaData.get(0) + qnaData.get(1) + qnaData.get(2)) / 4;
        int oil = (qnaData.get(3) + qnaData.get(5) + qnaData.get(7)) / 4;
        int sense = (qnaData.get(6) + qnaData.get(7) + qnaData.get(8)) / 4;
        int wrinkle = (qnaData.get(9) + qnaData.get(10) + qnaData.get(11)) / 4;
        int pole = (int) ((qnaData.get(4) + qnaData.get(5)) * 1.5) / 4;

        Intent resultIntent = new Intent();
        resultIntent.putExtra("water", water);
        resultIntent.putExtra("oil", oil);
        resultIntent.putExtra("sense", sense);
        resultIntent.putExtra("wrinkle", wrinkle);
        resultIntent.putExtra("pole", pole);
        setResult(SUCCESS_CALL_QNA, resultIntent);
        finish();
    }

    @Override
    public void onPageDataChanged(Page page) {
        String key = page.getKey();
        String value = page.getData().getString(SIMPLE_DATA_KEY);

        qnaData.put(keyToIndex(key), valueToIndex(value));

        if (page.isRequired()) {
            if (recalculateCutOffPage()) {
                mPagerAdapter.notifyDataSetChanged();
                updateBottomBar();
            }
        }
    }

    @Override
    public Page onGetPage(String key) {
        return mWizardModel.findByKey(key);
    }

    private boolean recalculateCutOffPage() {
        // Cut off the pager adapter at first required page that isn't completed
        int cutOffPage = mCurrentPageSequence.size() + 1;
        for (int i = 0; i < mCurrentPageSequence.size(); i++) {
            Page page = mCurrentPageSequence.get(i);
            if (page.isRequired() && !page.isCompleted()) {
                cutOffPage = i;
                break;
            }
        }

        if (mPagerAdapter.getCutOffPage() != cutOffPage) {
            mPagerAdapter.setCutOffPage(cutOffPage);
            return true;
        }

        return false;
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        private int mCutOffPage;
        private Fragment mPrimaryItem;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i >= mCurrentPageSequence.size()) {
                return new ReviewFragment();
            }

            return mCurrentPageSequence.get(i).createFragment();
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO: be smarter about this
            if (object == mPrimaryItem) {
                // Re-use the current fragment (its position never changes)
                return POSITION_UNCHANGED;
            }

            return POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position,
                                   Object object) {
            super.setPrimaryItem(container, position, object);
            mPrimaryItem = (Fragment) object;
        }

        @Override
        public int getCount() {
            return Math.min(mCutOffPage + 1, mCurrentPageSequence == null ? 1
                    : mCurrentPageSequence.size() + 1);
        }

        public void setCutOffPage(int cutOffPage) {
            if (cutOffPage < 0) {
                cutOffPage = Integer.MAX_VALUE;
            }
            mCutOffPage = cutOffPage;
        }

        public int getCutOffPage() {
            return mCutOffPage;
        }
    }
}