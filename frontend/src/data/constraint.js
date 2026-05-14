export const tags = [
  { id: 1, tag_name: '安静', category: '氛围' },
  { id: 2, tag_name: '治愈', category: '氛围' },
  { id: 3, tag_name: '放松', category: '氛围' },
  { id: 4, tag_name: '独处', category: '功能' },
  { id: 5, tag_name: '发呆', category: '功能' },
  { id: 6, tag_name: '思考', category: '功能' },
  { id: 7, tag_name: '惬意', category: '氛围' },
  { id: 8, tag_name: '隐秘', category: '氛围' },
]

export const Recommend = {
  code: 200,
  data: {
    understanding: '懂了，你需要安静地待一会儿',
    memory_matches: [
      {
        place_id: 1,
        place_name: '沙河公园湖边长椅',
        address: '南山区沙河西路',
        mood_tags: ['安静', '放空', '独处'],
        crowd_level: '低',
        one_sentence: '下午三点有阳光，通常没人',
        image_url: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=peaceful%20lake%20side%20bench%20in%20park%20with%20sunlight%20afternoon&size=landscape_16_9',
        distance_text: '距你1.2km',
        match_type: 'visited',
        match_reason: '你上次说真的很放松',
        last_visited: '2026-03-15'
      }
    ],
    emotion_matches: [
      {
        place_id: 2,
        place_name: '华侨城旧书店',
        address: '南山区华侨城创意园',
        mood_tags: ['安静', '独处'],
        crowd_level: '低',
        one_sentence: '复古书架，咖啡香气',
        image_url: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=vintage%20old%20bookstore%20with%20wooden%20shelves%20warm%20lighting&size=landscape_16_9',
        distance_text: '距你2.5km',
        match_type: 'new',
        match_reason: '适合安静阅读'
      },
      {
        place_id: 3,
        place_name: '南头古城后院',
        address: '南山区南头古城',
        mood_tags: ['隐秘', '绿意'],
        crowd_level: '低',
        one_sentence: '青砖古墙，绿植环绕',
        image_url: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=ancient%20city%20backyard%20with%20green%20plants%20stone%20walls&size=landscape_16_9',
        distance_text: '距你3.8km',
        match_type: 'new',
        match_reason: '静谧的历史角落'
      }
    ]
  }
}

export const PlaceDetail = {
  1: {
    place_id: 1,
    place_name: '沙河公园湖边长椅',
    address: '南山区沙河西路',
    latitude: 22.5532,
    longitude: 113.9456,
    mood_tags: ['安静', '放空', '独处'],
    crowd_level: '低',
    best_time: '工作日下午',
    one_sentence: '下午三点有阳光，通常没人',
    full_description: '位于沙河公园北侧湖边，紧邻大草坪。长椅面朝湖水，视野开阔，是放松身心的绝佳去处。',
    image_url: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=peaceful%20lake%20side%20bench%20in%20park%20with%20sunlight%20afternoon&size=landscape_16_9',
    tips: '蚊虫较多，建议带驱蚊水',
    your_history: {
      has_visited: true,
      visit_count: 2,
      last_visited: '2026-03-15',
      your_rating: 5,
      your_feedback: '真的很放松'
    }
  },
  2: {
    place_id: 2,
    place_name: '华侨城旧书店',
    address: '南山区华侨城创意园',
    latitude: 22.5431,
    longitude: 113.9526,
    mood_tags: ['安静', '独处', '阅读'],
    crowd_level: '低',
    best_time: '上午10点到下午4点',
    one_sentence: '复古书架，咖啡香气',
    full_description: '位于华侨城创意园内，是一家充满怀旧氛围的旧书店。店内有大量二手书籍，搭配咖啡香气，是阅读和思考的好地方。',
    image_url: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=vintage%20old%20bookstore%20with%20wooden%20shelves%20warm%20lighting&size=landscape_16_9',
    tips: '周末人较多，建议工作日前往',
    your_history: {
      has_visited: false,
      visit_count: 0,
      last_visited: null,
      your_rating: null,
      your_feedback: null
    }
  },
  3: {
    place_id: 3,
    place_name: '南头古城后院',
    address: '南山区南头古城',
    latitude: 22.5389,
    longitude: 113.9287,
    mood_tags: ['隐秘', '绿意', '历史'],
    crowd_level: '低',
    best_time: '傍晚时分',
    one_sentence: '青砖古墙，绿植环绕',
    full_description: '南头古城内一处隐秘的后院，青砖古墙与茂密绿植相映成趣，让人仿佛穿越回古代，是感受历史沉淀的好地方。',
    image_url: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=ancient%20city%20backyard%20with%20green%20plants%20stone%20walls&size=landscape_16_9',
    tips: '建议穿舒适的鞋子，地面不平',
    your_history: {
      has_visited: false,
      visit_count: 0,
      last_visited: null,
      your_rating: null,
      your_feedback: null
    }
  }
}

export const MemoryList = {
  code: 200,
  data: {
    list: [
      {
        memory_id: 1,
        place_id: 1,
        place_name: '沙河公园湖边长椅',
        image_url: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=peaceful%20lake%20side%20bench%20in%20park&size=landscape_16_9',
        interaction_type: 'VISITED',
        rating: 5,
        feedback: '真的很放松',
        visited_at: '2026-03-15'
      },
      {
        memory_id: 2,
        place_id: 2,
        place_name: '华侨城旧书店',
        image_url: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=vintage%20old%20bookstore&size=landscape_16_9',
        interaction_type: 'BOOKMARKED',
        rating: 4,
        feedback: '很有氛围',
        visited_at: '2026-03-10'
      }
    ]
  }
}

export const mood_tags = [
  { id: 1, tag_name: '烦闷', color: 'bg-gray-200 text-gray-700' },
  { id: 2, tag_name: '想被治愈', color: 'bg-green-100 text-green-700' },
  { id: 3, tag_name: '枯槁', color: 'bg-amber-50 text-amber-600' },
  { id: 4, tag_name: '需要烟火气', color: 'bg-orange-100 text-orange-700' },
  { id: 5, tag_name: '安静', color: 'bg-teal-50 text-teal-600' },
]

export const memoryList = [
  {
    id: 1,
    date: '2024.12.23',
    title: '沙河公园湖边长椅',
    image: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=peaceful%20lake%20side%20bench%20in%20park%20with%20trees%20sunset&size=portrait_4_3',
    tags: ['下午', '湖边', '放空']
  },
  {
    id: 2,
    date: '2024.12.20',
    title: '旧图书馆窗边',
    image: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=old%20library%20window%20seat%20with%20sunlight%20reading%20books&size=portrait_4_3',
    tags: ['阅读', '阳光', '安静']
  },
  {
    id: 3,
    date: '2024.12.18',
    title: '老街幽深巷',
    image: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=ancient%20old%20street%20alley%20with%20lanterns%20evening&size=portrait_4_3',
    tags: ['漫步', '古风', '回忆']
  }
]

export const notifications = [
  {
    icon: '☔',
    title: '天气提醒',
    content: '可能会下雨，请记得带伞'
  },
  {
    icon: '🌧',
    title: '天气提醒',
    content: '可能会下雨，请记得带伞'
  },
  {
    icon: '✨',
    title: '神秘彩蛋',
    content: '大树附近藏着一枚彩蛋，快去找找看吧！'
  }
]
