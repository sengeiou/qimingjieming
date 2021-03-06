package com.tjyw.atom.network.model;

import com.tjyw.atom.network.result.RetroResultItem;
import com.tjyw.atom.network.utils.ArrayUtil;

import java.util.List;

/**
 * Created by stephen on 17-8-23.
 */
public class NameDefinition implements RetroResultItem {

    private static final long serialVersionUID = 3037741131798324414L;

    public int id;

    public List<NameCharacter> wordsList;

    public NameScore nameScore;

    public String name;

    public boolean favorite;

    // surname、day、gender 在收藏列表和订单列表会附带(用于点击后进入解名详情页的请求参数)
    public String surname;

    public String day;

    public int gender;

    public String getGivenName() {
        if (ArrayUtil.isEmpty(wordsList)) {
            return null;
        } else {
            StringBuilder givenName = new StringBuilder();
            int size = wordsList.size();
            for (int i = 0; i < size; i ++) {
                NameCharacter character = wordsList.get(i);
                if (null != character) {
                    if (character.isGivenCharacter()) {
                        givenName.append(character.word);
                    }
                }
            }

            return givenName.toString();
        }
    }

    public String getSurname() {
        if (ArrayUtil.isEmpty(wordsList)) {
            return null;
        } else {
            StringBuilder surname = new StringBuilder();
            int size = wordsList.size();
            for (int i = 0; i < size; i ++) {
                NameCharacter character = wordsList.get(i);
                if (null != character) {
                    if (! character.isGivenCharacter()) {
                        surname.append(character.word);
                    }
                }
            }

            return surname.toString();
        }
    }
}
