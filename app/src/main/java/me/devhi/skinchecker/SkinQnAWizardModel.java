package me.devhi.skinchecker;

import android.content.Context;

import com.tech.freak.wizardpager.model.AbstractWizardModel;
import com.tech.freak.wizardpager.model.PageList;
import com.tech.freak.wizardpager.model.SingleFixedChoicePage;

public class SkinQnAWizardModel extends AbstractWizardModel {
    public SkinQnAWizardModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        return new PageList(
                new SingleFixedChoicePage(this, "세안 후 아무것도 안 바르면 피부가 땅기는 느낌이 들어요.")
                        .setChoices("매우 그렇다", "그렇다", "보통이다", "아니다", "매우 아니다")
                        .setRequired(true),
                new SingleFixedChoicePage(this, "날씨가 건조하면 각질이 일어나는 편이에요.")
                        .setChoices("매우 그렇다", "그렇다", "보통이다", "아니다", "매우 아니다")
                        .setRequired(true),
                new SingleFixedChoicePage(this, "보습제를 발라도 3~4시간이 지나면 피부가 땅기거나 푸석푸석해요.")
                        .setChoices("매우 그렇다", "그렇다", "보통이다", "아니다", "매우 아니다")
                        .setRequired(true),
                new SingleFixedChoicePage(this, "오후가 되면 이마와 볼 부분이 번들거려요.")
                        .setChoices("매우 그렇다", "그렇다", "보통이다", "아니다", "매우 아니다")
                        .setRequired(true),
                new SingleFixedChoicePage(this, "이마와 코(T존) 부위의 모공이 크고 도드라져요.")
                        .setChoices("매우 그렇다", "그렇다", "보통이다", "아니다", "매우 아니다")
                        .setRequired(true),
                new SingleFixedChoicePage(this, "코와 턱 주위에 블렉헤드나 여드름이 자주 생겨요.")
                        .setChoices("매우 그렇다", "그렇다", "보통이다", "아니다", "매우 아니다")
                        .setRequired(true),
                new SingleFixedChoicePage(this, "화장품을 바꾸면 트러블이 생겨요.")
                        .setChoices("매우 그렇다", "그렇다", "보통이다", "아니다", "매우 아니다")
                        .setRequired(true),
                new SingleFixedChoicePage(this, "스트레스를 받거나 과음했을 때 뾰루지가 나요.")
                        .setChoices("매우 그렇다", "그렇다", "보통이다", "아니다", "매우 아니다")
                        .setRequired(true),
                new SingleFixedChoicePage(this, "자외선, 미세먼지, 환절기 등 외부 요인에 따라 피부가 가렵거나 따가울 때가 있어요.")
                        .setChoices("매우 그렇다", "그렇다", "보통이다", "아니다", "매우 아니다")
                        .setRequired(true),
                new SingleFixedChoicePage(this, "피부가 얇고 탄력이 없는 편이에요.")
                        .setChoices("매우 그렇다", "그렇다", "보통이다", "아니다", "매우 아니다")
                        .setRequired(true),
                new SingleFixedChoicePage(this, "눈가나 입가에 잔주름이 있어요.")
                        .setChoices("매우 그렇다", "그렇다", "보통이다", "아니다", "매우 아니다")
                        .setRequired(true),
                new SingleFixedChoicePage(this, "턱선이나 얼굴 살이 처진 느낌이에요.")
                        .setChoices("매우 그렇다", "그렇다", "보통이다", "아니다", "매우 아니다")
                        .setRequired(true)
        );
    }
}