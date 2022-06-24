package com.study.forum.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wy
 * @version 1.0
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 敏感词替代符
    private static final String REPLACE_WORD = "***";

    private TrieNode rootNode = new TrieNode();

    // 当容器实例化(服务启动时), bean在构造器执行之后
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-word.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = bufferedReader.readLine()) != null) {
                this.addKeyWord(keyword);
            }
        } catch (Exception e) {
            logger.error("读入敏感词文件失败...{}", e);
        }
    }

    public void addKeyWord(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            Character c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            // 在树中没找到，新建节点
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.setSubNode(c, subNode);
            }

            // 指向子节点
            tempNode = subNode;

            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        // 遍历前缀树
        TrieNode tempNode = rootNode;
        // 遍历
        int begin = 0;
        int pos = 0;
        // 结果
        StringBuilder sb = new StringBuilder();
        while (pos < text.length()) {
            char c = text.charAt(pos);
            // 跳过符号
            if (isSymbol(c)) {
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                pos++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 在树中未找到
                sb.append(text.charAt(begin));
                begin++;
                pos = begin;
                // 重新回到树的根
                tempNode = rootNode;
            } else if (tempNode.isKeyWordEnd()) {
                // 到达敏感词末尾，说明找到了
                sb.append(REPLACE_WORD);
                begin = ++pos;
                tempNode = rootNode;
            } else {
                // 没到树尾
                pos++;
            }
        }
        // 从begin截取加入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    public class TrieNode {
        // 是否尾结点
        private boolean isKeyWordEnd = false;
        // <字符，子节点>
        private Map<Character, TrieNode> subNode = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        /* 获取子节点 */
        public TrieNode getSubNode(Character c) {
            return subNode.get(c);
        }

        /* 添加子节点 */
        public void setSubNode(Character c, TrieNode node) {
            subNode.put(c, node);
        }
    }
}
